package com.koi.rpc.server;

import com.koi.rpc.protocol.RpcRequest;
import com.koi.rpc.protocol.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;


/**
 * @author whuang
 * @date 2019/12/26
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private RpcResponse handleRpcRequest(RpcRequest rpcRequest) throws Exception {
        String className = rpcRequest.getClassName();
        // 根据接口名称获取服务对象
        Object serviceBean = RpcHandlerManager.handlerMap.get(className);
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = rpcRequest.getMethodName();
        Class<?>[] paremeterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();
        // jdk反射调用方法
        Method method = serviceClass.getMethod(methodName, paremeterTypes);
        method.setAccessible(true);
        Object result = method.invoke(serviceBean, parameters);
        RpcResponse response = new RpcResponse();
        response.setRequestId(rpcRequest.getRequestId());
        response.setResult(result);
        return response;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        RpcResponse rpcResponse = handleRpcRequest(rpcRequest);

        // 写入结果并关闭连接
        ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);
    }
}
