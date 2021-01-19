package kr.co.jaso.membase;

import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.MemcachedClient;

public class BasicMembaseOperation {
  public static void main(String[] args) {
    MemcachedClient mc = null;
    try {
      // (1)번 항목
      URI base = new URI("http://localhost:8091/pools");
      ArrayList<URI> baseURIs = new ArrayList<URI>();
      baseURIs.add(base);
      // (2)번 항목
      mc = new MemcachedClient(baseURIs, "mytest", "mytest", "passwd", true);
    } catch (Exception e) {
      e.printStackTrace();
    }

    Future<Boolean> future = mc.set("key", 0, "value");
    boolean result = false;
    try {
      result = future.get(1000, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      future.cancel(true);
    } catch (ExecutionException e) {
      future.cancel(true);
    } catch (InterruptedException e) {
      future.cancel(true);
    }

    System.out.println("result is " + result);

    mc.shutdown();
  }
}
