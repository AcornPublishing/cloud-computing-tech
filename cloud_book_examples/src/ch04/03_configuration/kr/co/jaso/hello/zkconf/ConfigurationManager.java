package kr.co.jaso.hello.zkconf;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

public class ConfigurationManager implements Watcher {
  private ZooKeeper zk;
  
  public ConfigurationManager() throws Exception {
    zk = new ZooKeeper(HelloServer.ZK_SERVERS, HelloServer.SESSION_TIMEOUT, this);
  }
  
  public void setConfValue(String key, String value) throws Exception {
    String confPath = HelloServer.CONF_PATH + "/" + key;
    if(zk.exists(confPath, false) == null) {
      zk.create(confPath, value.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    } else {
      zk.setData(confPath, value.getBytes(), -1);
      System.out.println("Data changed:" + confPath + "," + value);
    }
    zk.create(HelloServer.CONF_PATH + "/bbb", null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
  }

  @Override
  public void process(WatchedEvent event) {
  }
  
  public static void main(String[] args) throws Exception {
    if(args.length < 2) {
      System.out.println("Usage: java ConfigurationManager <key> <value>");
      System.exit(0);
    }
    (new ConfigurationManager()).setConfValue(args[0], args[1]);
  }
}
