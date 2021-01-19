package kr.co.jaso.hello.zk;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import kr.co.jaso.hello.zk.generated.HelloService;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class HelloClient implements Watcher {
  private static final String HELLO_SERVER = "/helloserver";
  private CountDownLatch connMonitor = new CountDownLatch(1);
  private Object nodeMonitor = new Object();
  private List<String> helloServers = new ArrayList<String>(1);
  private ZooKeeper zk;
  private Random rand = new Random();
  private HelloServerWatcher helloServerWatcher = new HelloServerWatcher();
  
  public void startClient() throws Exception {
    zk = new ZooKeeper("127.0.0.1:2181", 10 * 1000, this);
    connMonitor.await();

    synchronized (nodeMonitor) {
      loadHelloServers();
      if (helloServers.size() == 0) {
        nodeMonitor.wait();
      }
    }

    while (true) {
      doSomething();
    }
  }

  private void loadHelloServers() throws Exception {
    helloServers.clear();
    System.out.println("Load HelloServer: " + HELLO_SERVER);
    helloServers.addAll(zk.getChildren(HELLO_SERVER, helloServerWatcher));
  }

  private void doSomething() throws Exception {
    String helloServer;
    synchronized (nodeMonitor) {
      if(helloServers.size() == 0) {
        System.out.println("No live server");
        Thread.sleep(5 * 1000);
        return;
      } else if (helloServers.size() == 1) {
        helloServer = helloServers.get(0);
      } else {
        helloServer = helloServers.get(rand.nextInt(helloServers.size() - 1));
      }
    }
    
    try {
      String[] serverInfos = helloServer.split(":");
      TSocket socket = new TSocket(serverInfos[0], Integer.parseInt(serverInfos[1]));
      socket.setTimeout(5 * 1000);
      TTransport transport = new TFramedTransport(socket);
      HelloService.Client client = new HelloService.Client(new TBinaryProtocol(transport));
  
      transport.open();
  
      String result = client.greeting("test01", 30);
      System.out.println("Received [" + result + "]");
  
      transport.close();
    } catch (Exception e) {
      System.out.println("Error to " + helloServer + ": " + e.getMessage());
    }
    Thread.sleep(5 * 1000);
  }

  @Override
  public void process(WatchedEvent event) {
    if (event.getType() == Event.EventType.None) {
      if (event.getState() == Event.KeeperState.SyncConnected) {
        connMonitor.countDown();
      }
    }
  }

  class HelloServerWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
      synchronized (nodeMonitor) {
        try {
          loadHelloServers();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    (new HelloClient()).startClient();
  }
}