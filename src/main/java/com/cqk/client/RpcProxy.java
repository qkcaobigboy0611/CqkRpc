/**
 * @author qkcao
 * @date 2023/7/30 07:48
 */
package com.cqk.client;

import com.alibaba.fastjson.JSON;
import com.cqk.bean.RpcRequest;
import com.cqk.bean.RpcResponse;
import com.cqk.registry.IServiceDiscovery;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RPC 代理（用于创建 RPC 服务代理）
 */
public class RpcProxy {
    private static final Logger LOG = Logger.getLogger(RpcProxy.class);

    private IServiceDiscovery iServiceDiscovery;

    private String serviceAddress;

    public RpcProxy(IServiceDiscovery iServiceDiscovery) {
        this.iServiceDiscovery = iServiceDiscovery;
    }

    public <T> T create(final Class<?> interfaceClass) {
        return create(interfaceClass, "");
    }


    public <T> T create(final Class<?> interfaceClass, final String serviceVersion) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // todo 1.封装请求参数
                        RpcRequest rpcRequest = new RpcRequest
                                .RequestBuilder(UUID.randomUUID().toString(),
                                // 获取方法声明类名的代码片段
                                method.getDeclaringClass().getName(),
                                method.getName(),
                                serviceVersion,
                                method.getParameterTypes(),
                                args).create();

                        // todo 2.获取接口的IP和端口
                        if (null != iServiceDiscovery) {
                            String serviceName = interfaceClass.getName();
                            if (StringUtils.isNotBlank(serviceVersion)) {
                                serviceName += "-" + serviceVersion;
                            }
                            serviceAddress = iServiceDiscovery.discover(serviceName);
                        }

                        if (StringUtils.isEmpty(serviceAddress)) {
                            throw new RuntimeException("server address is empty");
                        }
                        String[] serviceAddressArray = StringUtils.splitByWholeSeparator(serviceAddress, ":");
                        String host = serviceAddressArray[0];
                        int port = Integer.parseInt(serviceAddressArray[1]);
                        RpcClient rpcClient = new RpcClient(host, port);
                        // todo 3.根据IP和端口 进行远程接口调用（也就是网络传输）
                        RpcResponse response = rpcClient.send(rpcRequest);
                        LOG.info("代理请求中 Request:" + JSON.toJSONString(rpcRequest) + ";Response:" + JSON.toJSONString(response));
                        if (null == response) {
                            throw new RuntimeException("response is null");
                        }
                        if (response.getException() != null) {
                            throw response.getException();
                        }
                        return response.getResult();

                    }
                });
    }
}
