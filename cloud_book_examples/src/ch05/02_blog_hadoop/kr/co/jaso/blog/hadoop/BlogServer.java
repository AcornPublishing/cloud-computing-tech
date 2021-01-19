package kr.co.jaso.blog.hadoop;

import java.net.InetAddress;

import kr.co.jaso.blog.hadoop.generated.BlogService;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;

public class BlogServer {
  public static final int PORT = 9099; 
  public static BlogServer blogServer;
  
  private String hostName;
  private BlogServiceHandler blogService;
  
  public BlogServer(String hdfsUri) throws Exception {
    blogService = new BlogServiceHandler(this, hdfsUri);
  }
  
  public String getHostName() {
    return hostName;
  }

  public int getPort() {
    return PORT;
  }
  
  public BlogServiceHandler getBlogService() {
    return blogService;
  }
  
  public void startBlogServer() throws Exception {
    startHttpServer();
    startThriftServer();
  }
  
  public void startHttpServer() throws Exception {
    BlogHttpServer server = new BlogHttpServer("webapps", 8080);
    server.start();
    System.out.println("BlogHttpServer started(port: 8080)");
  }
  
  public void startThriftServer() throws Exception {
    this.hostName = InetAddress.getLocalHost().getHostName();
    
    final TNonblockingServerSocket socket = new TNonblockingServerSocket(PORT);
    final BlogService.Processor processor = new BlogService.Processor(blogService);
    final TServer server = new THsHaServer(processor, socket,
        new TFramedTransport.Factory(), new TBinaryProtocol.Factory());

    System.out.println("BlogServer started(port: " + PORT + ")");
    server.serve();
  }
  
  public static void main(String[] args) throws Exception {
    if(args.length < 1) {
      System.out.println("Usage java BlogServer <hdfs>");
      System.out.println("  hdfs option: hdfs://127.0.0.1:9000");
      System.exit(0);
    }
    blogServer = new BlogServer(args[0]);
    blogServer.startBlogServer();
  }
}
