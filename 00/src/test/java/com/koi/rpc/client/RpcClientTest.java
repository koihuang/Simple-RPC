package com.koi.rpc.client;

import com.koi.rpc.api.HelloService;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author whuang
 * @date 2019/12/18
 */
public class RpcClientTest {

    @Test
    public void testHello() {
        RpcClient rpcClient = new RpcClient("127.0.0.1",8866);
        HelloService helloService = rpcClient.createRpcProxy(HelloService.class);
        String result = helloService.hello("huang");
        Assert.assertEquals("hello huang",result);
    }
}