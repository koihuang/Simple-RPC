package com.koi.rpc.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @author whuang
 * @date 2019/12/18
 */
public class RpcServerTest {

    public static void main(String[] args) throws Exception {
        new ClassPathXmlApplicationContext("server-spring.xml");
    }
}