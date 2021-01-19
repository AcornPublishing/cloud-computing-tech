package kr.co.jaso.zk.basic;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class WatcherTest implements Watcher {
  private static final String ZK_HOSTS = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
  private static final int ZK_SESSION_TIMEOUT = 10 * 1000;
  private ZooKeeper zk;

  private CountDownLatch connMonitor = new CountDownLatch(1);
  private WatcherTest() throws Exception {
    zk = new ZooKeeper(ZK_HOSTS, ZK_SESSION_TIMEOUT, this);
    connMonitor.await();
    SampleNodeWatcher nodeWatcher = new SampleNodeWatcher();
    zk.exists("/tes01", nodeWatcher);
  }

  private void exec() {
    while (true) {
      try {
        Thread.sleep(5 * 1000);
        long sid = zk.getSessionId();
        byte[] passwd = zk.getSessionPasswd();
        zk.close();
        zk = new ZooKeeper(ZK_HOSTS, ZK_SESSION_TIMEOUT, this, sid, passwd);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void process(WatchedEvent event) {
    if (event.getType() == Event.EventType.None) {
      switch (event.getState()) {
      case SyncConnected:
        System.out.println("ZK SyncConnected");
        connMonitor.countDown();
        break;
      case Disconnected:
        System.out.println("ZK Disconnected");
        break;
      case Expired:
        System.out.println("ZK Session Expired");
        break;
      }
    }
  }

  public static void main(String[] args) throws Exception {
    WatcherTest test = new WatcherTest();
    test.exec();
  }
}

class SampleNodeWatcher implements Watcher {
  @Override
  public void process(WatchedEvent event) {
    if (event.getType() == Event.EventType.NodeCreated) {
      System.out.println("Receive NodeCreated event: " + event.getPath());
    } else if (event.getType() == Event.EventType.NodeDeleted) {
      System.out.println("Receive NodeDeleted event: " + event.getPath());
    } else if (event.getType() == Event.EventType.NodeDataChanged) {
      System.out.println("Receive NodeDataChanged event: " + event.getPath());
    } else if (event.getType() == Event.EventType.NodeChildrenChanged) {
      System.out.println("Receive NodeChildrenChanged event: " + event.getPath());
    }
  }
}
