package kr.co.jaso.blog.thrift;

import java.nio.ByteBuffer;
import java.util.List;

import kr.co.jaso.blog.thrift.generated.BlogArticle;
import kr.co.jaso.blog.thrift.generated.BlogService;
import kr.co.jaso.blog.thrift.generated.ServiceStatus;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class BlogClient {
  public static void main(String[] args) throws Exception {
    int timeout = 10 * 1000;
    final TSocket socket = new TSocket("127.0.0.1", BlogServer.PORT);
    socket.setTimeout(timeout);
    
    final TTransport transport = new TFramedTransport(socket);
    final TProtocol protocol = new TBinaryProtocol(transport);
    final BlogService.Client client = new BlogService.Client(protocol);

    transport.open();

    ServiceStatus serverStatus = client.getServiceStatus();
    System.out.println(serverStatus.getHostName() + ":" + serverStatus.getPort() + 
        " status " + serverStatus.getStatus());
    
    BlogArticle article = new  BlogArticle();
    article.setUserId("user01");
    article.setTitle("Blog thrift example");
    article.setContents(ByteBuffer.wrap("Blog thrift example is very easy".getBytes()));
    
    client.saveBlog(article);

    article = new  BlogArticle();
    article.setUserId("user01");
    article.setTitle("Hello thrift example");
    article.setContents(ByteBuffer.wrap("Hello thrift example is very easy".getBytes()));
    
    client.saveBlog(article);

    List<BlogArticle> blogArticles = client.searchBlogByUserId("user01");
    System.out.println("=======================================");
    for(BlogArticle eachArticle: blogArticles) {
      System.out.println(eachArticle.getTitle());
    }
    System.out.println("=======================================");
    transport.close();
  }
}
