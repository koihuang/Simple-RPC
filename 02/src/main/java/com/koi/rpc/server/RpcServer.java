package com.koi.rpc.server;


import com.koi.rpc.protocol.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author whuang
 * @date 2019/12/16
 */
public class RpcServer {

    private int SERVER_PORT;

    // 线程池
    private ThreadPoolExecutor threadPoolExecutor;

    public RpcServer() {
    }

    public RpcServer(int SERVER_PORT) {
        this.SERVER_PORT = SERVER_PORT;
    }

    //服务名与服务对象map
    private Map<String, Object> handlerMap = new HashMap<>();


    public void start() throws IOException, ClassNotFoundException {
        // 初始化线程池
        initialThreadPool();
        // 开启服务端socket
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

        // 监听请求
        while (true) {
            final Socket socket = serverSocket.accept();
            // 提交到线程池处理
            threadPoolExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    RpcResponse response = new RpcResponse();
                    try {
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        RpcRequest rpcRequest = (RpcRequest) ois.readObject();
                        System.out.println("接收请求 rpcRequest = " + rpcRequest);
                        response.setRequestId(rpcRequest.getRequestId());

                        //处理请求
                        Object result = handleRequest(rpcRequest);
                        response.setResult(result);
                    } catch (Exception e) {
                        response.setError(e.getMessage());
                        e.printStackTrace();
                    }

                    try {
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        writeResponse2SocketAndClose(socket, response, oos);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    private void initialThreadPool() {

        int nthreads = Runtime.getRuntime().availableProcessors() * 2;
        threadPoolExecutor = new ThreadPoolExecutor(nthreads, nthreads, 600L,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void addService(Class<?> interfaceClazz, Object serviceBean) {
        handlerMap.put(interfaceClazz.getName(), serviceBean);
    }


    public void register(Class<?> interfaceClass, String registryServerIp, int registryServerPort) throws IOException {
        Socket socket = new Socket(registryServerIp, registryServerPort);
        ServiceRegistryRequest serviceAddress = new ServiceRegistryRequest(UUID.randomUUID().toString(), ServiceRegistryRequestType.REGISTRY, interfaceClass.getName(), InetAddress.getLocalHost().getHostAddress() + ":" + SERVER_PORT);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        writeResponse2SocketAndClose(socket, serviceAddress, oos);
    }


    private Object handleRequest(RpcRequest rpcRequest) throws Exception {

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
        return method.invoke(serviceBean, parameters);
    }


    private void writeResponse2SocketAndClose(Socket socket, Object response, ObjectOutputStream oos) throws IOException {
        oos.writeObject(response);
        oos.flush();
        oos.close();
        socket.close();
    }

}
