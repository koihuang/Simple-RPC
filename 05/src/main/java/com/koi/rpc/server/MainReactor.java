package com.koi.rpc.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 *
 * 请求接收MainReactor,负载均衡发给subReactor
 * @author whuang
 * @date 2019/12/26
 */
public class MainReactor {

    SubReactor[] subReactors;

    private final int SERVER_PORT;

    private int nextSubReatorIndex = 0;

    public MainReactor(int subReactorNum,int SERVER_PORT) {
        this.SERVER_PORT = SERVER_PORT;
        this.subReactors = new SubReactor[subReactorNum];
        for (int i = 0; i < subReactorNum; i++) {
            this.subReactors[i] = new SubReactor();
        }
    }

    public void start() throws IOException {
        // 开启线程运行subreators
        for (SubReactor subReactor : this.subReactors) {
            new Thread(subReactor).start();
        }

        // 开始接收请求
        beginAcceptRequest();
    }

    public void beginAcceptRequest() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 开启服务端socket
        serverSocketChannel.bind(new InetSocketAddress(SERVER_PORT));

        // 监听请求
        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            this.subReactors[this.nextSubReatorIndex++].addSocketToQueue(socketChannel);
            if (this.nextSubReatorIndex == this.subReactors.length) {
                this.nextSubReatorIndex = 0;
            }
        }
    }

}
