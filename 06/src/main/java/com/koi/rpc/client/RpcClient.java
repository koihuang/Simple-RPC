package com.koi.rpc.client;

import com.koi.rpc.protocol.*;

import java.io.IOException;
import java.io.InputStream;
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
                        NettyClient nettyClient = new NettyClient();
                        RpcResponse rpcResponse = nettyClient.send(request,serverIp,serverPort);
                        return rpcResponse.getResult();
                    }
                });
    }

    public RpcFuture asyncCall(final Class<?> interfaceClass,final String method,final Object... args) {

        final RpcFuture rpcFuture = new RpcFuture();

        // 开启新线程处理
        new Thread(() -> {

            try {
                // 获取服务地址
                ServiceRegistryResponse serviceRegistryResponse = getServerAddress(interfaceClass,serviceRegistryIp,serviceRegistryPort);
                String serverAddress = serviceRegistryResponse.getServerAddress();
                String[] serverAddressSplit = serverAddress.split(":");
                String serverIp = serverAddressSplit[0];
                int serverPort = Integer.parseInt(serverAddressSplit[1]);

                // 封装请求
                RpcRequest request = createRpcRequest(interfaceClass, method, args);
                // 发出请求
                NettyClient nettyClient = new NettyClient();
                RpcResponse response = nettyClient.send(request,serverIp,serverPort);
                // 接收结果
                // 插入数据到future中
                rpcFuture.done(response);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }).start();

        return rpcFuture;
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


    private RpcRequest createRpcRequest(Class<?> interfaceClass, String method, Object[] args) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(interfaceClass.getName());
        request.setMethodName(method);
        request.setParameters(args);
        Class[] parameterTypes = new Class[args.length];
        // Get the right class type
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = getClassType(args[i]);
        }
        request.setParameterTypes(parameterTypes);
        return request;
    }

    private Class<?> getClassType(Object obj) {
        Class<?> classType = obj.getClass();
        String typeName = classType.getName();
        switch (typeName) {
            case "java.lang.Integer":
                return Integer.TYPE;
            case "java.lang.Long":
                return Long.TYPE;
            case "java.lang.Float":
                return Float.TYPE;
            case "java.lang.Double":
                return Double.TYPE;
            case "java.lang.Character":
                return Character.TYPE;
            case "java.lang.Boolean":
                return Boolean.TYPE;
            case "java.lang.Short":
                return Short.TYPE;
            case "java.lang.Byte":
                return Byte.TYPE;
        }

        return classType;
    }
}
