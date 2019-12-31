package com.koi.rpc.server;

import java.util.HashMap;
import java.util.Map;

/**
 * @author whuang
 * @date 2019/12/26
 */
public class RpcHandlerManager {

    // 本项目中不会有并发写的情况,所以HashMap就够用了
    public static final Map<String, Object> handlerMap = new HashMap<>();
}
