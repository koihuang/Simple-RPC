package com.koi.rpc.registry;

import com.koi.rpc.protocol.ServiceRegistryRequest;
import com.koi.rpc.protocol.ServiceRegistryRequestType;
import com.koi.rpc.protocol.ServiceRegistryResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author whuang
 * @date 2019/12/16
 */
public class ServiceRegistry {

    private Map<String, List<String>> serviceAddressManager = new ConcurrentHashMap<>();

    public int PORT;

    public ServiceRegistry(int PORT) {
        this.PORT = PORT;
    }

    public ServiceRegistry() {
    }

    private AtomicInteger roundRobin = new AtomicInteger(0);

    public void start() throws IOException, ClassNotFoundException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ServiceRegistryRequest serviceRegistryRequest = (ServiceRegistryRequest) ois.readObject();
            switch (serviceRegistryRequest.getType()) {
                case ServiceRegistryRequestType.REGISTRY:
                    System.out.println("接收到注册请求:"+serviceRegistryRequest);
                    register(serviceRegistryRequest);
                    break;
                case ServiceRegistryRequestType.DISCOVER:
                    System.out.println("接收服务发现请求:"+serviceRegistryRequest);
                    response(socket,serviceRegistryRequest);
                    break;
                default:
                    responseWithErro(socket,serviceRegistryRequest,"未知请求类型");
            }
            ois.close();
            socket.close();
        }
    }

    private void responseWithErro(Socket socket, ServiceRegistryRequest serviceRegistryRequest,String error) throws IOException {
        ServiceRegistryResponse response = new ServiceRegistryResponse();
        response.setRequestId(serviceRegistryRequest.getRequestId());
        response.setError(error);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(response);
        oos.flush();
        oos.close();

    }

    private void response(Socket socket, ServiceRegistryRequest serviceRegistryRequest) throws IOException {
        if (serviceAddressManager.containsKey(serviceRegistryRequest.getServiceName())) {
            List<String> serviceAddressList = serviceAddressManager.get(serviceRegistryRequest.getServiceName());
            int size = serviceAddressList.size();
            if (size != 0) {
                int index = (roundRobin.getAndAdd(1) + size) % size;
                String serviceAddress = serviceAddressList.get(index);
                ServiceRegistryResponse response = new ServiceRegistryResponse();
                response.setServerAddress(serviceAddress);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(response);
                oos.flush();
                oos.close();
            }

        }
    }

    private void register(ServiceRegistryRequest serviceRegistryRequest) {
        List<String> serviceAddressList;
        if (serviceAddressManager.containsKey(serviceRegistryRequest.getServiceName())) {
            serviceAddressList = serviceAddressManager.get(serviceRegistryRequest.getServiceName());
        } else {
            serviceAddressList = new ArrayList<>();
        }
        if (!serviceAddressList.contains(serviceRegistryRequest.getServerAddress())) {
            serviceAddressList.add(serviceRegistryRequest.getServerAddress());
        }
        serviceAddressManager.put(serviceRegistryRequest.getServiceName(), serviceAddressList);
    }

}
