package com.koi.rpc.server;

import com.koi.rpc.protocol.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author whuang
 * @date 2019/12/16
 */
public class RpcServer {

    private int SERVER_PORT;

    public RpcServer() {
    }

    public RpcServer(int SERVER_PORT) {
        this.SERVER_PORT = SERVER_PORT;
    }

    //服务名与服务对象map
    private Map<String, Object> handlerMap = new HashMap<>();


    public void start() throws IOException, ClassNotFoundException {

        // 开启服务端socket
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

        // 监听请求
        while (true) {
            Socket socket = serverSocket.accept();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();
            System.out.println("接收请求 rpcRequest = " + rpcRequest);
            RpcResponse response = new RpcResponse();
            response.setRequestId(rpcRequest.getRequestId());
            try {
                //处理请求
                Object result = handleRequest(rpcRequest);
                response.setResult(result);
            } catch (Exception e) {
                response.setError(e.getMessage());
                e.printStackTrace();
            }
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            writeResponse2SocketAndClose(socket, response, oos);
        }
    }

    public void addService(Class<?> interfaceClazz, Object serviceBean) {
        handlerMap.put(interfaceClazz.getName(),serviceBean);
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
        return method.invoke(serviceBean,parameters);
    }


    private void writeResponse2SocketAndClose(Socket socket, Object response, ObjectOutputStream oos) throws IOException {
        oos.writeObject(response);
        oos.flush();
        oos.close();
        socket.close();
    }

}
