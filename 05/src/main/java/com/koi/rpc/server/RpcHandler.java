package com.koi.rpc.server;

import com.koi.rpc.protocol.RpcRequest;
import com.koi.rpc.protocol.RpcResponse;
import com.koi.rpc.utils.ObjectBytesUtil;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * @author whuang
 * @date 2019/12/26
 */
public class RpcHandler implements Runnable{

    private Selector writeSelector;

    private RpcRequest rpcRequest;

    private RpcSocket rpcSocket;

    public RpcHandler(Selector writeSelector,RpcSocket rpcSocket, RpcRequest rpcRequest) {
        this.writeSelector = writeSelector;
        this.rpcRequest = rpcRequest;
        this.rpcSocket = rpcSocket;
    }

    @Override
    public void run() {
        try {
            RpcResponse rpcResponse = handleRpcRequest(this.rpcRequest);
            byte[] bytes = ObjectBytesUtil.objectToBytes(rpcResponse);
            this.rpcSocket.setWriteBuffer(ByteBuffer.allocate(bytes.length));
            this.rpcSocket.getWriteBuffer().put(bytes);
            // 注册socketChannel到writerselector
            this.rpcSocket.getSocketChannel().register(this.writeSelector, SelectionKey.OP_WRITE, this.rpcSocket);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private RpcResponse handleRpcRequest(RpcRequest rpcRequest) throws Exception {
        String className = rpcRequest.getClassName();
        // 根据接口名称获取服务对象
        Object serviceBean = RpcHandlerManager.handlerMap.get(className);
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
