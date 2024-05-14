/**
 * @author qkcao
 * @date 2023/7/26 16:34
 */
package com.cqk.registry.impl;

import com.alibaba.fastjson.JSON;
import com.cqk.common.constants.ZookeeperGlobalConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * 连接ZK
 */
@Slf4j
public class ZookeeperConnection {
    private static final Logger LOG = Logger.getLogger(ZookeeperConnection.class);
    private static CountDownLatch latch = new CountDownLatch(1);
    private static ZooKeeper zooKeeper = null;

    public static ZooKeeper connectionZk(String zkAddress) {
        try {
            zooKeeper = new ZooKeeper(zkAddress, ZookeeperGlobalConstants.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    // SyncConnected表示客户端和服务器的某一个节点建立连接，并完成一次version，zxid的同步
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        log.info("成功连接上ZK，address={}" + zkAddress + ",zookeeper={}" + JSON.toJSONString(zooKeeper));
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (Exception e) {
            log.error("ZookeeperConnection.connectionZk is error:{}" + e.getMessage());
        }
        return zooKeeper;
    }

}
