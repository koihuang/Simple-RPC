package com.koi.rpc.server;

import com.koi.rpc.protocol.RpcRequest;
import com.koi.rpc.utils.ObjectBytesUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author whuang
 * @date 2019/12/26
 */
public class SubReactor implements Runnable {

    private static final ThreadPoolExecutor threadPool =  new ThreadPoolExecutor(16, 16, 600L,
            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536),
            new ThreadPoolExecutor.CallerRunsPolicy());;

    private Queue<SocketChannel> inboundSocketQueue = new ArrayBlockingQueue(1024);

    private Selector readSelector;

    private Selector writeSelector;

    private long nextSocketId = 0;

    private ByteBuffer readByteBuffer = ByteBuffer.allocate(1024 * 1024);


    // 初始化读写selector
    {

        try {
            this.readSelector = Selector.open();
            this.writeSelector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSocketToQueue(SocketChannel socketChannel) {
        this.inboundSocketQueue.add(socketChannel);
    }

    @Override
    public void run() {
        try {
            while (true) {
                executeCycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeCycle() throws Exception {
        // 接收请求
        takeNewSockets();
        // 读取和处理请求
        readAndHandleSockets();
        // 相应结果
        writeToSockets();
    }

    private void takeNewSockets() throws IOException {
        SocketChannel socketChannel = inboundSocketQueue.poll();
        while (socketChannel != null) {
            socketChannel.configureBlocking(false);
            SelectionKey key = socketChannel.register(this.readSelector, SelectionKey.OP_READ);
            RpcSocket socket = new RpcSocket(nextSocketId++, socketChannel);
            key.attach(socket);
            socketChannel = inboundSocketQueue.poll();
        }
    }

    private void readAndHandleSockets() throws Exception {
        int ready = this.readSelector.selectNow();
        if (ready > 0) {
            Set<SelectionKey> selectionKeys = this.readSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                readAndHandleSocket(key);
                keyIterator.remove();
            }
        }
    }

    private void readAndHandleSocket(SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        RpcSocket socket = (RpcSocket) key.attachment();
        int readBytes = channel.read(this.readByteBuffer);
        while (readBytes > 0) {
            this.readByteBuffer.flip();
            while (this.readByteBuffer.hasRemaining()) {
                socket.getReadBuffer().add(this.readByteBuffer.get());
            }
            this.readByteBuffer.clear();
            readBytes = channel.read(this.readByteBuffer);

        }
        this.readByteBuffer.clear();

        // 如果读到数据末尾,则处理请求
        if (readBytes == -1) {
            RpcRequest rpcRequest = (RpcRequest) ObjectBytesUtil.bytesToObject(socket.getReadBuffer());
            System.out.println("接收到请求: rpcRequest = " + rpcRequest);
            key.attach(null);
            key.cancel();

            // rpc请求的处理交给线程池
            threadPool.execute(new RpcHandler(this.writeSelector,socket,rpcRequest));
        }
    }

    private void writeToSockets() throws Exception {
        int ready = this.writeSelector.selectNow();
        if (ready > 0) {
            Set<SelectionKey> selectionKeys = this.writeSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                writeToSocket(key);
                keyIterator.remove();
            }
        }
    }


    private void writeToSocket(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        RpcSocket socket = (RpcSocket) key.attachment();
        socket.getWriteBuffer().flip();
        int writeBytes = channel.write(socket.getWriteBuffer());
        while (writeBytes > 0 && socket.getWriteBuffer().hasRemaining()) {
            writeBytes = channel.write(socket.getWriteBuffer());
        }
        //数据写完了
        if (!socket.getWriteBuffer().hasRemaining()) {
            key.attach(null);
            key.channel().close();
            key.cancel();
        }
    }
}
