package com.koi.rpc.server;

import com.koi.rpc.api.HelloService;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectInput;


/**
 * @author whuang
 * @date 2019/12/18
 */
public class RpcServerTest {

    public static void main(String[] args) throws Exception {
        RpcServer server = new RpcServer(8866);
        server.addService(HelloService.class,new HelloServiceImpl());
        //注册到注册中心
        server.register(HelloService.class,"127.0.0.1",8000);
        server.start();

    }
}