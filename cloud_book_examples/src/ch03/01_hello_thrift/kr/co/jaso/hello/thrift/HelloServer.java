package kr.co.jaso.hello.thrift;

import kr.co.jaso.hello.thrift.generated.HelloService;

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.THsHaServer.Options;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;

public class HelloServer {
  public void startServer(int port) throws Exception {
    Options options = new Options();
    options.workerThreads = 2000;
    
    final TNonblockingServerSocket socket = new TNonblockingServerSocket(port);
    final HelloService.Processor processor = new HelloService.Processor(
        new HelloHandler());
    final TServer server = new THsHaServer(new TProcessorFactory(processor), socket,
        new TFramedTransport.Factory(), new TBinaryProtocol.Factory(), options);

    System.out.println("HelloServer started(port:" + port + ")");
    server.serve();
  }
  
  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Usage java HelloServer <port>");
      System.exit(0);
    }
    
    (new HelloServer()).startServer(Integer.parseInt(args[0]));
  }
}
