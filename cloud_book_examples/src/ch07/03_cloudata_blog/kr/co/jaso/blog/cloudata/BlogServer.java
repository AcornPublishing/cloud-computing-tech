package kr.co.jaso.blog.cloudata;

import java.net.InetAddress;

import kr.co.jaso.blog.cloudata.dao.CloudataBlogDAO;
import kr.co.jaso.blog.cloudata.generated.BlogService;

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
  
  public BlogServer() throws Exception {
    blogService = new BlogServiceHandler(this, new CloudataBlogDAO());
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
    blogServer = new BlogServer();
    blogServer.startThriftServer();
  }
}
