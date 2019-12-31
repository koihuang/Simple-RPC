package com.koi.rpc.server;

import com.koi.rpc.api.HelloService;

/**
 * @author whuang
 * @date 2019/12/17
 */
@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
