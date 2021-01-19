package kr.co.jaso.blog.thrift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.jaso.blog.thrift.generated.BlogArticle;
import kr.co.jaso.blog.thrift.generated.BlogException;
import kr.co.jaso.blog.thrift.generated.BlogService;
import kr.co.jaso.blog.thrift.generated.ServiceStatus;

import org.apache.thrift.TException;

public class BlogServiceHandler implements BlogService.Iface {
  //테스트 용도로 로직을 간단하게 하기 위해 메모리에만 저장
  //일반적인 경우에는 DB 또는 파일에 저장
  static Map<String, BlogArticle> blogArticles = new HashMap<String, BlogArticle>(); 
  
  private BlogServer server;
  public BlogServiceHandler(BlogServer server) {
    this.server = server;
  }
  
  @Override
  public BlogArticle getBlog(String userId, long articleId) throws BlogException, TException {
    synchronized(blogArticles) {
      return blogArticles.get(userId + "_" + articleId);
    }
  }

  @Override
  public void saveBlog(BlogArticle article) throws TException {
    synchronized(blogArticles) {
      article.setArticleId(System.currentTimeMillis() + System.nanoTime());
      blogArticles.put(article.getUserId() + "_" + article.getArticleId(), article);
    }
  }

  @Override
  public List<BlogArticle> searchBlogByUserId(String userId) throws BlogException, TException {
    List<BlogArticle> result = new ArrayList<BlogArticle>();
    
    synchronized(blogArticles) {
      for(Map.Entry<String, BlogArticle> entry: blogArticles.entrySet()) {
        String key = entry.getKey();
        if(userId.equals(key.substring(0, key.indexOf("_")))) {
          result.add(entry.getValue());
        }
      }
    }
    return result;
  }

  @Override
  public ServiceStatus getServiceStatus() throws TException {
    ServiceStatus serviceStatus = new ServiceStatus();
    
    serviceStatus.setHostName(server.getHostName());
    serviceStatus.setPort(server.getPort());
    serviceStatus.setStatus("OK");
    
    return serviceStatus;
  }
}
