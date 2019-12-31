package com.koi.rpc.client;

import com.koi.rpc.protocol.RpcResponse;

/**
 * @author whuang
 * @date 2019/12/25
 */
public interface RpcCallBack {
    void success(RpcResponse result);
}
