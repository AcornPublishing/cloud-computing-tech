package kr.co.jaso.queue;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

public class Producer implements Watcher {
  public static final String QUEUE_PREFIX = "q_";
  public static final String SERVICE_NAME = "twitter_crawl";
  public static final String QUEUE_NAME = "test";

  private String zkServers;
  private String producerId;
  
  private ZooKeeper zk;
  private Object mutex = new Object();
  
  public Producer(String zkServers, String producerId) throws Exception {
    this.zkServers = zkServers;
    this.producerId = producerId;
  }
  
  public void startProducer() { 
    try {
      zk = new ZooKeeper(zkServers, 10 * 1000, this);

      synchronized (mutex) {
        mutex.wait();
      }
      
      System.out.println("Start producer:" + producerId);
      
      String queuePath =  "/" + SERVICE_NAME + "/" + QUEUE_NAME + "/" + QUEUE_PREFIX;
      
      long seqNum = 1;    //Consumer에 전달하는 데이터
      while(true) {
        try {
          zk.create(queuePath, (producerId + seqNum).getBytes(), 
              Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
          seqNum++;
        } catch (KeeperException.ConnectionLossException e) {
          synchronized (mutex) {
            mutex.wait();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }      
  }
  
  public void process(WatchedEvent event) {
    synchronized(mutex) {
      mutex.notify();
    }
  }

  public static void createQueueRoot(ZooKeeper zk) throws Exception {
    try { 
      zk.create("/" + SERVICE_NAME, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    } catch (KeeperException.NodeExistsException e) {
    }
    try { 
      zk.create("/" + SERVICE_NAME + "/" + QUEUE_NAME, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    } catch (KeeperException.NodeExistsException e) {
    }
  }
}
