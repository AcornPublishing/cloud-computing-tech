package kr.co.jaso.hello.zkconf;

import kr.co.jaso.hello.zkconf.generated.HelloService;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class HelloClient {
  public static void main(String[] args) throws Exception {
    args = new String[]{"127.0.0.1", "8888", "babokim", "50"};
    
    if (args.length < 4) {
      System.out.println("Usage java HelloClient <host> <port> <name> <age>");
      System.exit(0);
    }
    String host = args[0];
    int port = Integer.parseInt(args[1]);
    int timeout = 10 * 1000;
    final TSocket socket = new TSocket(host, port);
    socket.setTimeout(timeout);
    final TTransport transport = new TFramedTransport(socket);
    final TProtocol protocol = new TBinaryProtocol(transport);
    final HelloService.Client client = new HelloService.Client(protocol);

    transport.open();

    String result = client.greeting(args[2], Integer.parseInt(args[3]));
    System.out.println("Received [" + result + "]");

    transport.close();
  }
}