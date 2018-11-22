package com.lmx.pushplatform.server;

import com.google.common.hash.Hashing;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class Registy {
    private static String REGIST_PATH = "/push";
    private static String host = System.getProperty("zk.hosts");
    private static final Logger LOGGER = LoggerFactory.getLogger(Registy.class);
    private static CountDownLatch latch = new CountDownLatch(1);

    public static void exposeApp() throws Exception {
        ZooKeeper zooKeeper = new ZooKeeper(host, 60 * 1000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    latch.countDown();
                }
                LOGGER.info("event={}", event);
            }
        });
        latch.await();
        if (zooKeeper.exists(REGIST_PATH, true) == null) {
            zooKeeper.create(REGIST_PATH, (Server.host + ":" + Server.port).getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        if (zooKeeper.exists(REGIST_PATH + "/apps", true) == null) {
            zooKeeper.create(REGIST_PATH + "/apps", "".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        String realPath = zooKeeper.create(REGIST_PATH + "/apps/"
                        + Hashing.sha1().hashBytes(UUID.randomUUID().toString().getBytes()), (Server.host + ":" + Server.port).getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        LOGGER.info("push-server published success,path={}", realPath);
    }
}
