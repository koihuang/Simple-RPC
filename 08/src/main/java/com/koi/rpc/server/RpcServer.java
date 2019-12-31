package com.koi.rpc.server;


import com.koi.rpc.protocol.*;
import com.koi.rpc.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

/**
 * @author whuang
 * @date 2019/12/16
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private String serviceAddress;
    private ServiceRegistry serviceRegistry;

    public RpcServer(String serviceAddress, ServiceRegistry serviceRegistry) {
        this.serviceAddress = serviceAddress;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> rpcbeansMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (rpcbeansMap != null) {
            for (Object serviceBean : rpcbeansMap.values()) {
                Class interfaceClass = serviceBean.getClass().getAnnotation(RpcService.class).value();
                addService(interfaceClass,serviceBean);
            }
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }


    public void start() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch)  {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new RpcDecoder(RpcRequest.class));
                    pipeline.addLast(new RpcEncoder(RpcResponse.class));
                    pipeline.addLast(new RpcHandler());
                }
            });
            bootstrap.option(ChannelOption.SO_BACKLOG,1024);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE,true);

            // 解析服务地址的ip和端口
            String[] addressArray = serviceAddress.split(":");
            String ip = addressArray[0];
            int port = Integer.parseInt(addressArray[1]);
            ChannelFuture future = bootstrap.bind(ip,port).sync();

            //注册服务到注册中心
            if (serviceRegistry != null) {
                for (String interfacename : RpcHandlerManager.handlerMap.keySet()) {
                    serviceRegistry.register(interfacename,serviceAddress);
                }
            }

            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    public void addService(Class<?> interfaceClazz, Object serviceBean) {
        RpcHandlerManager.handlerMap.put(interfaceClazz.getName(), serviceBean);
    }


}
