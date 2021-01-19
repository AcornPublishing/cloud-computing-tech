package kr.co.jaso.membase;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionObserver;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;

public class ClassicMemcachedOperation {
  public static void main(String[] args) {
    ConnectionFactoryBuilder cfb = new ConnectionFactoryBuilder();
    final CountDownLatch latch = new CountDownLatch(1);

    ConnectionObserver observer = new ConnectionObserver() {

      @Override
      public void connectionLost(SocketAddress arg0) {
      }

      @Override
      public void connectionEstablished(SocketAddress arg0, int arg1) {
        latch.countDown();

      }
    };

    cfb.setInitialObservers(Collections.singleton(observer));
    // (1)번 항목
    AuthDescriptor ad = new AuthDescriptor(new String[] { "PLAIN" }, new PlainCallbackHandler("mytest", "passwd"));
    cfb.setAuthDescriptor(ad);
    cfb.setDaemon(true);
    // (2)번 항목
    cfb.setProtocol(Protocol.BINARY);

    MemcachedClient client = null;

    try {
      // (2)번 항목
      client = new MemcachedClient(cfb.build(), AddrUtil.getAddresses("192.168.0.1:11211"));
      latch.await();
    } catch (Exception e) {
      e.printStackTrace();
    }

    Future<Boolean> f = client.set("key", 0, "value");

    try {
      boolean result = f.get(1000, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
