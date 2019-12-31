package com.koi.rpc.server;

import com.koi.rpc.api.HelloService;
import org.junit.Test;

import java.io.IOException;


/**
 * @author whuang
 * @date 2019/12/18
 */
public class RpcServerTest {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        RpcServer server = new RpcServer(8866);
        server.addService(HelloService.class,new HelloServiceImpl());
        server.start();
    }
}