package com.koi.rpc.client;

import com.koi.rpc.protocol.RpcDecoder;
import com.koi.rpc.protocol.RpcEncoder;
import com.koi.rpc.protocol.RpcRequest;
import com.koi.rpc.protocol.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


/**
 * @author whuang
 * @date 2019/12/27
 */
public class NettyClient extends SimpleChannelInboundHandler<RpcResponse> {

    private RpcResponse response;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        this.response = msg;
    }

    public RpcResponse send(RpcRequest request,String serverIp,int serverPort) throws InterruptedException {

        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new RpcEncoder(RpcRequest.class));
                pipeline.addLast(new RpcDecoder(RpcResponse.class));
                pipeline.addLast(NettyClient.this);
            }
        });
        bootstrap.option(ChannelOption.TCP_NODELAY,true);
        ChannelFuture future = bootstrap.connect(serverIp, serverPort).sync();
        Channel channel = future.channel();
        channel.writeAndFlush(request).sync();
        channel.closeFuture().sync();
        return response;
    }

}
