package com.koi.rpc.client;

import com.koi.rpc.protocol.RpcRequest;
import com.koi.rpc.protocol.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.UUID;

/**
 * @author whuang
 * @date 2019/12/16
 */
public class RpcClient {

    private String SERVER_IP;
    private int SERVER_PORT;

    public RpcClient(String SERVER_IP, int SERVER_PORT) {
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
    }

    public RpcClient() {
    }

    @SuppressWarnings("unchecked")
    public  <T> T createRpcProxy(final Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setClassName(interfaceClass.getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);
                        RpcResponse response = sendRpcRequest(request);
                        return response.getResult();
                    }
                });
    }

    private RpcResponse sendRpcRequest(RpcRequest request) throws IOException, ClassNotFoundException {
        RpcResponse response = null;
        //发起请求
        Socket socket = new Socket(SERVER_IP, SERVER_PORT);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(request);
        //接收请求结果
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        response = (RpcResponse) ois.readObject();

        oos.close();
        ois.close();
        socket.close();

        return response;
    }


}
