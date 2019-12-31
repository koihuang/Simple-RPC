package com.koi.rpc.protocol;

/**
 * @author whuang
 * @date 2019/12/25
 */
public class RpcFuture {


    private RpcResponse result;

    private volatile boolean done = false;

    private RpcCallBack rpcCallBack;

    public synchronized void done(RpcResponse result) {
        if (this.done) {
            return;
        }
        this.result = result;
        this.done = true;
        notifyAll();
        if (rpcCallBack != null) {
            rpcCallBack.success(result);
        }
    }


    public synchronized Object asyncGetResult() {
        while (!done) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.result.getResult();
    }
    public RpcCallBack getRpcCallBack() {
        return rpcCallBack;
    }

    public void setRpcCallBack(RpcCallBack rpcCallBack) {
        this.rpcCallBack = rpcCallBack;
    }

    public RpcResponse getResult() {
        return result;
    }

    public void setResult(RpcResponse result) {
        this.result = result;
    }
}
