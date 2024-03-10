/**
 * @author qkcao
 * @date 2023/7/26 16:22
 */
package com.cqk.registry.impl;

import com.cqk.common.constants.ZookeeperGlobalConstants;
import com.cqk.registry.IServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;

/**
 * 实现服务注册
 */
@Slf4j
public class ZookeeperServiceRegistryImpl extends ZookeeperConnection implements IServiceRegistry {
    private static final Logger LOG = Logger.getLogger(ZookeeperServiceRegistryImpl.class);

    private final ZooKeeper zk;

    public ZookeeperServiceRegistryImpl (String zkAddress) {
        zk = ZookeeperConnection.connectionZk(zkAddress);
    }

    @Override
    public void register(String serviceName, String serviceAddress) {
        try {
            String rootPath = ZookeeperGlobalConstants.ZK_ROOT_REGISTRY_ZNODE_PATH;

            // 创建一个目录节点
            Stat exists1 = zk.exists(rootPath, false);
            if(exists1 == null) {
                createNode(rootPath);
                LOG.info("创建一个目录节点,路径为:" + rootPath);
            }
            // 创建一个子目录节点
            String serviceNamePath = rootPath + "/" + serviceName;
            Stat exists2 = zk.exists(serviceNamePath, false);
            if(exists2 == null) {
                createNode(serviceNamePath);
                LOG.info("创建一个子目录节点,路径为=" + serviceNamePath);
            }
            String serviceAddressPath = serviceNamePath + "[zookeeper:" + serviceAddress + "]";
            Stat exists3 = zk.exists(serviceAddressPath, false);
            if(exists3 == null) {
                createNodeData(serviceAddressPath, serviceAddress);
                LOG.info("创建一个子目录下的子目录节点,路径为=" + serviceAddressPath + "数据为=" + serviceAddress);
            }
        } catch (Exception e) {
            LOG.error("register is error={}" + e.getMessage()+"serviceName={}" + serviceName + "serviceAddress={}" + serviceAddress);
        }
    }

    public void createNode(String rootPath) {
        try {
            Stat s = zk.exists(rootPath, false);
            if (s == null) {
                zk.create(rootPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            LOG.error("createNode is error={}" + e.getMessage()+"serviceName={}" + rootPath);
        }
    }
    public void createNodeData(String addressPath, String serviceAddress) {
        try {
            Stat s = zk.exists(addressPath, false);
            if (s == null) {
                byte[] bytes = serviceAddress.getBytes(StandardCharsets.UTF_8);
                zk.create(addressPath, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            }
        } catch (Exception e) {
            LOG.error("createNode is error={}" + e.getMessage()+"serviceName={}" + addressPath);
        }
    }
}
