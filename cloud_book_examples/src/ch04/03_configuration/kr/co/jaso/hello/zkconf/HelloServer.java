package kr.co.jaso.hello.zkconf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import kr.co.jaso.hello.zkconf.generated.HelloService;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

/**
 * 소켓 서버를 구성하는 클래스로 main 메소드를 가지고 있다.
 */
public class HelloServer implements Watcher {
  public static final String ZK_SERVERS = "127.0.0.1:2181";
  public static final int SESSION_TIMEOUT = 10 * 1000;
  public static final String HELLO_SERVICE_PATH = "/hello_service";
  public static final String CONF_PATH = HELLO_SERVICE_PATH + "/conf";
  public static final String CONF_GREETING_KEY = "hello.greeting";
  
  public static Map<String, String> configurations = new HashMap<String, String>();
  
  private ZooKeeper zk;
  private CountDownLatch connMonitor = new CountDownLatch(1);
  
  public HelloServer() throws Exception {
    zk = new ZooKeeper(ZK_SERVERS, SESSION_TIMEOUT, this);
    connMonitor.await();
    
    if(zk.exists(CONF_PATH, false) == null) {
      try {
        zk.create(HELLO_SERVICE_PATH, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      } catch (KeeperException.NodeExistsException e) {
        //ignore
      }
      zk.create(CONF_PATH, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    List<String> confNodes = zk.getChildren(CONF_PATH, this);
    for(String eachNode: confNodes) {
      byte[] confData = zk.getData(CONF_PATH + "/" + eachNode, this, new Stat());
      if(confData != null) {
        configurations.put(eachNode, new String(confData));
      }
    }
  }
  
  public void startServer(int port) throws Exception {
    final TNonblockingServerSocket socket = new TNonblockingServerSocket(port);
    final HelloService.Processor processor = new HelloService.Processor(
        new HelloHandler());
    final TServer server = new THsHaServer(processor, socket,
        new TFramedTransport.Factory(), new TBinaryProtocol.Factory());

    System.out.println("HelloServer started(port:" + port + ")");
    server.serve();
  }

  @Override
  public void process(WatchedEvent event) {
    if(event.getType() == Event.EventType.None) {
      if(event.getState() == Event.KeeperState.SyncConnected) {
        connMonitor.countDown();
      }
    } else if(event.getType() == Event.EventType.NodeChildrenChanged) {
      if(event.getPath().equals(CONF_PATH)) {
        reloadConfigurations();
      }
    } else if (event.getType() == Event.EventType.NodeDataChanged) {
      if(event.getPath().startsWith(CONF_PATH)) {
        String path = event.getPath();
        if(path.lastIndexOf("/") >= 0) {
          path = path.substring(path.lastIndexOf("/") + 1);
        }
        try {
          byte[] confData = zk.getData(event.getPath(), this, new Stat());
          synchronized(configurations) {
            if(confData != null) {
              configurations.put(path, new String(confData));
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  private void reloadConfigurations() {
    try {
      List<String> children = zk.getChildren(CONF_PATH, this);

      List<String> addedNodes = new ArrayList<String>();
      List<String> removedNodes = new ArrayList<String>();
      
      findChangedChildren(configurations.keySet(), children, addedNodes, removedNodes);
      
      synchronized(configurations) {
        for(String eachNode: removedNodes) {
          configurations.remove(eachNode);
        }
        
        for(String eachNode: addedNodes) {
          byte[] confData = zk.getData(CONF_PATH + "/" + eachNode, false, new Stat());
          if(confData != null) {
            configurations.put(eachNode, new String(confData));
          }
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public static void findChangedChildren(
      Collection<String> cachedDatas,
      Collection<String> currentChildren,
      Collection<String> addedNodes, 
      Collection<String> removedNodes
      ) throws Exception {
    Set<String> cachedSet = new HashSet<String>();
    cachedSet.addAll(cachedDatas);
    
    for(String eachNode: currentChildren) {
      if(cachedSet.remove(eachNode) == false) {
        removedNodes.add(eachNode);
      }
    }
    
    //add added consumer
    addedNodes.addAll(cachedSet);
  }
  
  public static void main(String[] args) throws Exception {
    args = new String[]{"8888"};
    if (args.length < 1) {
      System.out.println("Usage java HelloServer <port>");
      System.exit(0);
    }
    
    (new HelloServer()).startServer(Integer.parseInt(args[0]));
  }
}

/**
 * IDL에서 정의한 인터페이스를 구현한 클래스
 */
class HelloHandler implements HelloService.Iface {

  @Override
  public String greeting(String name, int age) throws TException {
    String greetingPrefix = "Hello";
    synchronized(HelloServer.configurations) {
      if(HelloServer.configurations.containsKey(HelloServer.CONF_GREETING_KEY)) {
        greetingPrefix = HelloServer.configurations.get(HelloServer.CONF_GREETING_KEY);
      }
    }
    return greetingPrefix + " " + name + ". You are " + age + " years old";
  }
}
