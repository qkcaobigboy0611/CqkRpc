/**
 * @author qkcao
 * @date 2023/7/26 16:18
 */
package com.cqk.registry;

// zk提供的服务注册接口
public interface IServiceRegistry {
    /**
     * 注册服务名称与服务地址
     */
    void register(String serviceName, String serviceAddress);
}
