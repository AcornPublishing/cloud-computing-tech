package kr.co.jaso.memcached;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionObserver;
import net.spy.memcached.FailureMode;
import net.spy.memcached.HashAlgorithm;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.ConnectionFactoryBuilder.Locator;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;

public class BasicTest {
  public static void main(String[] args) {
    String key = "test:key1";
    String value = "value1";

    ConnectionFactoryBuilder cfb = new ConnectionFactoryBuilder();
    cfb.setFailureMode(FailureMode.Cancel);
    cfb.setDaemon(true);
    cfb.setShouldOptimize(true);
    cfb.setLocatorType(Locator.CONSISTENT);
    cfb.setHashAlg(HashAlgorithm.KETAMA_HASH);
    cfb.setProtocol(Protocol.TEXT);
    cfb.setMaxReconnectDelay(10);

    // (1)번 항목
    final CountDownLatch latch = new CountDownLatch(1);
    final ConnectionObserver obs = new ConnectionObserver() {
      public void connectionEstablished(SocketAddress sa, int reconnectCount) {
        latch.countDown();
      }

      public void connectionLost(SocketAddress sa) {
        assert false : "Connection is failed.";
      }
    };

    // (2)번 항목
    cfb.setInitialObservers(Collections.singleton(obs));

    MemcachedClient mc = null;

    try {
      mc = new MemcachedClient(cfb.build(), AddrUtil.getAddresses("localhost:11211"));
    } catch (IOException e) {
      assert false : "Connection has some problem.";
    }
    // (3)번 항목
    try {
      latch.await();
    } catch (InterruptedException e1) {
    }

    // exptime이 100초인 키/값 데이터를 저장한다. 이 작업은 바로 완료되지 않고
    // Future 객체의 콜백을 통해서 나중에 전달받게 된다.
    Future<Boolean> setFuture = mc.set(key, 100, value);
    try {
      boolean setResult = setFuture.get(1000, TimeUnit.MILLISECONDS);
      System.out.println(setResult);
    } catch (Exception e) {
      System.out.println("Memcached Set Exception");
    }

    // 특정 키에 해당하는 값을 가져오게 된다.
    // Future 객체의 콜백을 통해서 나중에 값을 전달받게 된다.
    Future<Object> getFuture = mc.asyncGet("test:key1");
    try {
      Object getResult = getFuture.get(1000, TimeUnit.MILLISECONDS);
      System.out.println(getResult);
    } catch (Exception e) {
      System.out.println("Memcached Get Exception");
    }

    // memcached connection을 종료한다.
    mc.shutdown();
  }
}
