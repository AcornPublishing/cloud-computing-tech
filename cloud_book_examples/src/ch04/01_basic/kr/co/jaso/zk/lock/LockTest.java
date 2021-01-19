package kr.co.jaso.zk.lock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

public class LockTest implements Watcher {
  public static final String LOCK_PATH = "/mylock";

  ZooKeeper zk;
  String lockKey;
  CountDownLatch connMonitor = new CountDownLatch(1);
  Object mutex = new Object();

  public LockTest() throws Exception {
    //zk = new ZooKeeper("127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183", 5 * 1000, this);
    zk = new ZooKeeper("127.0.0.1:2181", 5 * 1000, this);
    if (zk.exists(LOCK_PATH, false) == null) {
      zk.create(LOCK_PATH, "".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }
  }

  public void testLock() throws Exception {
    while (true) {
      if (acquireLock()) {
        System.out.println("Get lock: key=" + lockKey);
        break;
      } else {
        System.out.println("Can't get lock: key=" + lockKey);
        synchronized (mutex) {
          zk.getChildren(LOCK_PATH, this);
          mutex.wait();
        }
      }
    }

    doSomething();
  }

  private void doSomething() throws Exception {
    while (true) {
      System.out.print("Type(q: 정상종료, x: 비정상종료):");
      String command = (new BufferedReader(new InputStreamReader(System.in))).readLine();
      if ("q".equals(command)) {
        releaseLock();
        zk.close();
        break;
      } else if ("x".equals(command)) {
        System.exit(0);
      }
    }
  }

  private boolean acquireLock() throws Exception {
    if (lockKey == null) {
      String createdPath = zk.create(LOCK_PATH + "/lock-", null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
      String[] tokens = createdPath.split("/");

      lockKey = tokens[tokens.length - 1];
    }

    List<String> children = zk.getChildren(LOCK_PATH, false);
    if (children == null || children.size() == 0) {
      throw new IOException("No child node:" + LOCK_PATH);
    }

    Collections.sort(children);

    return children.get(0).equals(lockKey);
  }

  private void releaseLock() throws Exception {
    zk.delete(LOCK_PATH + "/" + lockKey, -1);
  }

  @Override
  public void process(WatchedEvent event) {
    if (event.getType() == Event.EventType.None) {
      if (event.getState() == Event.KeeperState.SyncConnected) {
        connMonitor.countDown();
      }
    } else if (event.getType() == Event.EventType.NodeChildrenChanged) {
      synchronized (mutex) {
        mutex.notify();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    (new LockTest()).testLock();
  }
}
