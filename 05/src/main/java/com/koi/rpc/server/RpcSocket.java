package com.koi.rpc.server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author whuang
 * @date 2019/12/23
 */
public class RpcSocket {
    private long socketId;
    private List<Byte> readBuffer = new ArrayList<>(4*1024);
    private ByteBuffer writeBuffer = null;
    private SocketChannel socketChannel;

    public RpcSocket(long socketId, SocketChannel socketChannel) {
        this.socketId = socketId;
        this.socketChannel = socketChannel;
    }

    public long getSocketId() {
        return socketId;
    }

    public void setSocketId(long socketId) {
        this.socketId = socketId;
    }

    public List<Byte> getReadBuffer() {
        return readBuffer;
    }

    public void setReadBuffer(List<Byte> readBuffer) {
        this.readBuffer = readBuffer;
    }

    public ByteBuffer getWriteBuffer() {
        return writeBuffer;
    }

    public void setWriteBuffer(ByteBuffer writeBuffer) {
        this.writeBuffer = writeBuffer;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}
