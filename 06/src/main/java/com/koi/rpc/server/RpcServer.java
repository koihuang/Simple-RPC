package com.koi.rpc.server;


import com.koi.rpc.protocol.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;

/**
 * @author whuang
 * @date 2019/12/16
 */
public class RpcServer {

    private final int SERVER_PORT;

    public RpcServer(int SERVER_PORT) {
        this.SERVER_PORT = SERVER_PORT;
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

            ChannelFuture future = bootstrap.bind(SERVER_PORT).sync();

            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    public void addService(Class<?> interfaceClazz, Object serviceBean) {
        RpcHandlerManager.handlerMap.put(interfaceClazz.getName(), serviceBean);
    }


    public void register(Class<?> interfaceClass, String registryServerIp, int registryServerPort) throws IOException {
        Socket socket = new Socket(registryServerIp, registryServerPort);
        ServiceRegistryRequest serviceAddress = new ServiceRegistryRequest(UUID.randomUUID().toString(), ServiceRegistryRequestType.REGISTRY, interfaceClass.getName(), InetAddress.getLocalHost().getHostAddress() + ":" + SERVER_PORT);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        writeResponsetToSocketAndClose(socket, serviceAddress, oos);
    }

    private void writeResponsetToSocketAndClose(Socket socket, Object response, ObjectOutputStream oos) throws IOException {
        oos.writeObject(response);
        oos.flush();
        oos.close();
        socket.close();
    }
}
