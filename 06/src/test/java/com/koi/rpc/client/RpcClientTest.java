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
        RpcClient rpcClient = new RpcClient("127.0.0.1",8000);
        HelloService helloService = rpcClient.createRpcProxy(HelloService.class);
        String result = helloService.hello("huang");
        Assert.assertEquals("hello huang",result);
    }

    @Test
    public void testAsyncCall() {
        RpcClient rpcClient = new RpcClient("127.0.0.1",8000);
        RpcFuture rpcFuture = rpcClient.asyncCall(HelloService.class, "hello", "huang");
        System.out.println("do something else");
        Assert.assertEquals("hello huang",rpcFuture.asyncGetResult());
    }

    @Test
    public void testAsyncCallBack() {
        RpcClient rpcClient = new RpcClient("127.0.0.1",8000);
        RpcFuture rpcFuture = rpcClient.asyncCall(HelloService.class, "hello", "huang");
        rpcFuture.setRpcCallBack(result -> {
            System.out.println("回调打印结果:"+ result);
        });
        System.out.println("do something else");
        Assert.assertEquals("hello huang",rpcFuture.asyncGetResult());
    }
}