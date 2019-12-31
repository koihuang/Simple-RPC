package com.koi.rpc.server;

import com.koi.rpc.protocol.RpcRequest;
import com.koi.rpc.protocol.RpcResponse;
import com.koi.rpc.utils.ObjectBytesUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

/**
 * @author whuang
 * @date 2019/12/23
 */
public class RpcProcessor implements Runnable {
    private Selector readSelector = null;
    private Selector writeSelector = null;
    private long nextSocketId = 0;
    private Queue<SocketChannel> inboundSocketQueue;
    private ByteBuffer readByteBuffer = ByteBuffer.allocate(1024 * 1024);
    //服务名与服务对象map
    private Map<String, Object> handlerMap;

    public RpcProcessor(Queue<SocketChannel> inboundSocketQueue, Map<String, Object> handlerMap) {
        this.inboundSocketQueue = inboundSocketQueue;
        this.handlerMap = handlerMap;
        initSelector();
    }


    private void initSelector() {
        try {
            readSelector = Selector.open();
            writeSelector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            RpcResponse rpcResponse = handleRpcRequest(rpcRequest);
            byte[] bytes = ObjectBytesUtil.objectToBytes(rpcResponse);
            socket.setWriteBuffer(ByteBuffer.allocate(bytes.length));
            socket.getWriteBuffer().put(bytes);
            key.attach(null);
            key.cancel();
            // 注册socketChannel到writerselector
            socket.getSocketChannel().register(this.writeSelector, SelectionKey.OP_WRITE, socket);
        }
    }

    private RpcResponse handleRpcRequest(RpcRequest rpcRequest) throws Exception {
        String className = rpcRequest.getClassName();
        // 根据接口名称获取服务对象
        Object serviceBean = handlerMap.get(className);
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = rpcRequest.getMethodName();
        Class<?>[] paremeterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();
        // jdk反射调用方法
        Method method = serviceClass.getMethod(methodName, paremeterTypes);
        method.setAccessible(true);
        Object result = method.invoke(serviceBean, parameters);
        RpcResponse response = new RpcResponse();
        response.setResult(result);
        return response;
    }

}
