package com.koi.rpc.protocol;

import java.io.Serializable;

/**
 * @author whuang
 * @date 2019/12/16
 */
public class ServiceRegistryRequest implements Serializable {
    private static final long serialVersionUID = 4578444203374241737L;

    private String requestId;
    private String type;
    private String serviceName;
    private String serverAddress;


    public ServiceRegistryRequest(String requestId, String type, String serviceName, String serverAddress) {
        this.requestId = requestId;
        this.type = type;
        this.serviceName = serviceName;
        this.serverAddress = serverAddress;
    }

    public ServiceRegistryRequest(String type, String serviceName, String serverAddress) {
        this.type = type;
        this.serviceName = serviceName;
        this.serverAddress = serverAddress;
    }

    public ServiceRegistryRequest() {
    }

    public ServiceRegistryRequest(String serviceName, String serverAddress) {
        this.serviceName = serviceName;
        this.serverAddress = serverAddress;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    @Override
    public String toString() {
        return "ServiceAddress{" +
                "serviceName='" + serviceName + '\'' +
                ", serverAddress='" + serverAddress + '\'' +
                '}';
    }
}
