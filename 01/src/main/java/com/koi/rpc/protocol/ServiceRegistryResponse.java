package com.koi.rpc.protocol;

import java.io.Serializable;

/**
 * @author whuang
 * @date 2019/12/17
 */
public class ServiceRegistryResponse implements Serializable {
    private String requestId;
    private String error;
    private String serviceName;
    private String serverAddress;


    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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
}
