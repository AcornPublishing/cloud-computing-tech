package kr.co.jaso.hello.avro;

import java.net.InetSocketAddress;

import kr.co.jaso.hello.avro.generated.Greeting;
import kr.co.jaso.hello.avro.generated.GreetingException;
import kr.co.jaso.hello.avro.generated.HelloService;

import org.apache.avro.ipc.AvroRemoteException;
import org.apache.avro.ipc.HttpServer;
import org.apache.avro.ipc.SocketServer;
import org.apache.avro.specific.SpecificResponder;
import org.apache.avro.util.Utf8;

public class HelloServer {
  public static class HelloServiceImpl implements HelloService {
    @Override
    public Greeting hello(Utf8 userName) throws AvroRemoteException, GreetingException {
      Greeting greeting = new Greeting();
      greeting.greetingMessage = new Utf8("hello " + userName);
      return greeting;
    }
  }

  private static void startHttpServer() throws Exception {
    SpecificResponder r = new SpecificResponder(HelloService.class, new HelloServiceImpl());
    final HttpServer server = new HttpServer(r, 9090);

    System.out.println("Server Started");
    synchronized (server) {
      try {
        server.wait();
      } catch (InterruptedException e) {
      }
    }
  }

  private static void startSocketServer() throws Exception {
    final SocketServer server = new SocketServer(new SpecificResponder(HelloService.class, new HelloServiceImpl()),
        new InetSocketAddress(9090));

    System.out.println("Server Started");
    synchronized (server) {
      try {
        server.wait();
      } catch (InterruptedException e) {
      }
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Usage: java HelloServer <type(http|socket)>");
      System.exit(0);
    }
    if ("socket".equals(args[0])) {
      startSocketServer();
    } else {
      startHttpServer();
    }
    System.out.println("Server Stoped");
  }
}
