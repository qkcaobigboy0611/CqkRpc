/**
 * @author qkcao
 * @date 2023/7/29 20:38
 */
package com.cqk.registry.impl;

import com.alibaba.fastjson.JSON;
import com.cqk.common.constants.ZookeeperGlobalConstants;
import com.cqk.registry.IServiceDiscovery;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 服务发现
 */
public class ZookeeperServiceDiscoveryImpl extends ZookeeperConnection implements IServiceDiscovery {
    private static final Logger LOG = Logger.getLogger(ZookeeperServiceDiscoveryImpl.class);
    private static Map<String, List<String>> servers = new ConcurrentHashMap<String, List<String>>();

    private ZooKeeper zk = null;

    public ZookeeperServiceDiscoveryImpl(String zkAddress) {
        // 连接zk
        zk = ZookeeperConnection.connectionZk(zkAddress);
        if (zk != null) {
            // todo服务发现要对节点进行监控
            watchNode(zk);
        }
    }

    @Override
    public String discover(String serviceName) {
        LOG.info("发现的服务名称=" + serviceName);
        String rootPath = ZookeeperGlobalConstants.ZK_ROOT_REGISTRY_ZNODE_PATH;
        String servicePath = rootPath + "/" + serviceName;
        List<String> adress = servers.get(servicePath);
        int size = adress.size();
        if (size == 1) {
            return adress.get(0);
        } else {
            // todo 随机换取
            return adress.get(ThreadLocalRandom.current().nextInt(size));
        }

    }

    private void watchNode(ZooKeeper zk) {
        try {
            Map<String, List<String>> servers = new ConcurrentHashMap<String, List<String>>();
            String rootPath = ZookeeperGlobalConstants.ZK_ROOT_REGISTRY_ZNODE_PATH;
            List<String> serviceNameList = zk.getChildren(rootPath, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    // 当子节点发生变化(当我停止zk也会触发该点)
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zk);
                    }
                }
            });
            if (CollectionUtils.isNotEmpty(serviceNameList)) {
                for (String serviceName : serviceNameList) {
                    String serviceNamePath = rootPath + "/" + serviceName;
                    List<String> addressList = zk.getChildren(serviceNamePath, new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                                watchNode(zk);
                            }
                        }
                    });
                    if (CollectionUtils.isNotEmpty(addressList)) {
                        LOG.error("serviceNamePath:" + serviceNamePath + " is empty.");
                        continue;
                    }
                    // tempAddress是节点下的数据
                    List<String> tempAddress = new ArrayList<String>();
                    for (String address : addressList) {
                        String addressPath = serviceNamePath + "/" + address;
                        byte[] bytes = zk.getData(addressPath, false, null);
                        tempAddress.add(new String(bytes, Charset.defaultCharset()));
                    }
                    servers.put(serviceNamePath, tempAddress);
                }
            }
            ZookeeperServiceDiscoveryImpl.servers = servers;
            LOG.info("服务发现的节点和节点下的数据:" + JSON.toJSONString(servers));
        } catch (Exception e) {
            LOG.info("watchNode is error=" + e.getMessage());
        }
    }

}
