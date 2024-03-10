/**
 * @author qkcao
 * @date 2023/7/26 16:38
 */
package com.cqk.common.constants;

public interface ZookeeperGlobalConstants {
    // zk连接超时时间
    int ZK_SESSION_TIMEOUT = 2000000;

    //服务在zk下的路径 节点路径
    String ZK_ROOT_REGISTRY_ZNODE_PATH = "/DongFangHongRPC-Servers";

}
