package com.koi.rpc.client;

import com.koi.rpc.protocol.*;
import com.koi.rpc.registry.ServiceRegistry;

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

    private String serviceRegistryIp;
    private int serviceRegistryPort;

    public RpcClient(String serviceRegistryIp, int serviceRegistryPort) {
        this.serviceRegistryIp = serviceRegistryIp;
        this.serviceRegistryPort = serviceRegistryPort;
    }

    public RpcClient() {
    }

    @SuppressWarnings("unchecked")
    public  <T> T createRpcProxy(final Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        // 从注册中心获取服务端地址
                        ServiceRegistryResponse serviceRegistryResponse = getServerAddress(interfaceClass,serviceRegistryIp,serviceRegistryPort);
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setClassName(interfaceClass.getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);

                        String serverAddress = serviceRegistryResponse.getServerAddress();
                        String[] serverAddressSplit = serverAddress.split(":");
                        String serverIp = serverAddressSplit[0];
                        int serverPort = Integer.parseInt(serverAddressSplit[1]);
                        RpcResponse rpcResponse = sendRpcRequest(request,serverIp,serverPort);
                        return rpcResponse.getResult();
                    }
                });
    }

    private ServiceRegistryResponse getServerAddress( Class<?> interfaceClass,String serviceRegistryIp, int serviceRegistryPort) throws IOException, ClassNotFoundException {
        //发起请求
        Socket socket = new Socket(serviceRegistryIp, serviceRegistryPort);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ServiceRegistryRequest request = new ServiceRegistryRequest(UUID.randomUUID().toString(), ServiceRegistryRequestType.DISCOVER,interfaceClass.getName(),null);
        oos.writeObject(request);
        //接收请求结果
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ServiceRegistryResponse response = (ServiceRegistryResponse) ois.readObject();
        oos.close();
        ois.close();
        socket.close();
        return response;
    }

    private RpcResponse sendRpcRequest(RpcRequest request,String serverIp,int serverPort) throws IOException, ClassNotFoundException {
        RpcResponse response = null;
        //发起请求
        Socket socket = new Socket(serverIp, serverPort);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(request);
        socket.shutdownOutput();
        //接收请求结果
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        response = (RpcResponse) ois.readObject();

        oos.close();
        ois.close();
        socket.close();

        return response;
    }


}
