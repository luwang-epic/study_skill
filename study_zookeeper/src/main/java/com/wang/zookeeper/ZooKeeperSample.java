package com.wang.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.Objects;

/**
 * 原生的zookeeper使用，一般使用封装好的curator框架来操作zk
 */
public class ZooKeeperSample {
    public static final String ZK_HOST = "localhost:2181";

    private ZooKeeper zkClient;


    public ZooKeeperSample() throws Exception {
        zkClient = new ZooKeeper(ZK_HOST, 2000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                // 默认监听器，其他地方如果比采用构造监听对象，而是采用true的方式监听，则使用这个监听器
            }
        });
    }

    public void createNode(String nodePath, String nodeData) throws Exception {
        String path = zkClient.create(nodePath, nodeData.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println("create result --->" + path);
    }

    public void deleteIfExist(String nodePath) throws Exception {
        Stat stat = zkClient.exists(nodePath, false);
        if (Objects.nonNull(stat)) {
            System.out.println(String.format("node[%s] exist and delete it", nodePath));
            zkClient.delete(nodePath, stat.getVersion());
        } else {
            System.out.println(String.format("node[%s] not exist...", nodePath));
        }
    }

    public void getNodeInfo(String nodePath) throws Exception {
        Stat stat = new Stat();
        byte[] data = zkClient.getData(nodePath, false, stat);
        System.out.println("node data --->" + new String(data));
        System.out.println("node stat --->" + stat);
    }


    public static void main(String[] args) throws Exception {
        ZooKeeperSample zooKeeperSample = new ZooKeeperSample();
        String nodePath = "/test";
        String nodeData = "test data";
        zooKeeperSample.deleteIfExist(nodePath);
        zooKeeperSample.createNode(nodePath, nodeData);
        zooKeeperSample.getNodeInfo(nodePath);
    }
}
