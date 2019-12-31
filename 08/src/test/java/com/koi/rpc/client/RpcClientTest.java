package com.koi.rpc.client;

import com.koi.rpc.api.HelloService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author whuang
 * @date 2019/12/18
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:client-spring.xml")
public class RpcClientTest {
    @Autowired
    private RpcClient rpcClient;

    @Test
    public void testHello() {
        HelloService helloService = rpcClient.createRpcProxy(HelloService.class);
        String result = helloService.hello("huang");
        Assert.assertEquals("hello huang",result);
    }

    @Test
    public void testAsyncCall() {
        RpcFuture rpcFuture = rpcClient.asyncCall(HelloService.class, "hello", "huang");
        System.out.println("do something else");
        Assert.assertEquals("hello huang",rpcFuture.asyncGetResult());
    }

    @Test
    public void testAsyncCallBack() {
        RpcFuture rpcFuture = rpcClient.asyncCall(HelloService.class, "hello", "huang");
        rpcFuture.setRpcCallBack(result -> {
            System.out.println("回调打印结果:"+ result);
        });
        System.out.println("do something else");
        Assert.assertEquals("hello huang",rpcFuture.asyncGetResult());
    }
}