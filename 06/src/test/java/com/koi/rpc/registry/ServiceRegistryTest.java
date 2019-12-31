package com.koi.rpc.registry;

import java.io.IOException;


/**
 * @author whuang
 * @date 2019/12/18
 */
public class ServiceRegistryTest {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServiceRegistry serviceRegistry = new ServiceRegistry(8000);
        serviceRegistry.start();
    }
}