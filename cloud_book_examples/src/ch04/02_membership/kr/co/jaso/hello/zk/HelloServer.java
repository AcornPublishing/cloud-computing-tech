package kr.co.jaso.hello.zk;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;

import kr.co.jaso.hello.zk.generated.HelloService;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

/**
 * 소켓 서버를 구성하는 클래스로 main 메소드를 가지고 있다.
 */
public class HelloServer implements Watcher { 
  static final int ZK_SESSION_TIMEOUT = 10 * 1000;      //10 secs 
  static final String ZK_SERVERS = "localhost:2181";     //zkserver:port 
  static final String HELLO_GROUP = "/helloserver"; 
   
  private ZooKeeper zk; 
  private CountDownLatch connMonitor = new CountDownLatch(1);
  
  public void runServer(int port) throws Exception { 
    final TNonblockingServerSocket socket = new TNonblockingServerSocket(port);
    final HelloService.Processor processor = new HelloService.Processor(
        new HelloHandler());
    final TServer server = new THsHaServer(processor, socket,
        new TFramedTransport.Factory(), new TBinaryProtocol.Factory());

    serverStarted(port); 
    System.out.println("HelloServer started(port:" + port + ")"); 
    server.serve(); 
  } 
  private void serverStarted(int port) throws Exception { 
    zk = new ZooKeeper(ZK_SERVERS, ZK_SESSION_TIMEOUT, this);
    connMonitor.await();
    if(zk.exists(HELLO_GROUP, false) == null) { 
      zk.create(HELLO_GROUP, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT); 
    } 
     
    String serverInfo = InetAddress.getLocalHost().getHostName() + ":" + port; 
    zk.create(HELLO_GROUP + "/" + serverInfo, null, 
        Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL); 
  } 

  public void process(WatchedEvent event) { 
    System.out.println("Receive ZK event:" + event);
    if(event.getType() == Event.EventType.None) {
      if(event.getState() == Event.KeeperState.SyncConnected) {
        connMonitor.countDown();
      }
    }
  } 
  
  public static void main(String[] args) throws Exception {
    (new HelloServer()).runServer(9092);
  }
} 
