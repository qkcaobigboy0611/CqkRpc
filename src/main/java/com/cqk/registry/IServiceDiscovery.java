/**
 * @author qkcao
 * @date 2023/7/29 20:38
 */
package com.cqk.registry;

/**
 * 服务发现
 */
public interface IServiceDiscovery {
    /**
     * 根据服务名称查询服务地址
     * @param serviceName	服务名称
     * @return 服务地址
     */
    String discover(String serviceName);
}
