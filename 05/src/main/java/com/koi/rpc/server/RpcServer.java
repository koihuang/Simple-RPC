package com.koi.rpc.server;


import com.koi.rpc.protocol.ServiceRegistryRequest;
import com.koi.rpc.protocol.ServiceRegistryRequestType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;

/**
 * @author whuang
 * @date 2019/12/16
 */
public class RpcServer {

    private final int SERVER_PORT;

    public RpcServer(int SERVER_PORT) {
        this.SERVER_PORT = SERVER_PORT;
    }


    public void start() throws IOException {
        MainReactor mainReactor = new MainReactor(6,SERVER_PORT);
        mainReactor.start();
    }

    public void addService(Class<?> interfaceClazz, Object serviceBean) {
        RpcHandlerManager.handlerMap.put(interfaceClazz.getName(), serviceBean);
    }


    public void register(Class<?> interfaceClass, String registryServerIp, int registryServerPort) throws IOException {
        Socket socket = new Socket(registryServerIp, registryServerPort);
        ServiceRegistryRequest serviceAddress = new ServiceRegistryRequest(UUID.randomUUID().toString(), ServiceRegistryRequestType.REGISTRY, interfaceClass.getName(), InetAddress.getLocalHost().getHostAddress() + ":" + SERVER_PORT);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        writeResponsetToSocketAndClose(socket, serviceAddress, oos);
    }

    private void writeResponsetToSocketAndClose(Socket socket, Object response, ObjectOutputStream oos) throws IOException {
        oos.writeObject(response);
        oos.flush();
        oos.close();
        socket.close();
    }
}
