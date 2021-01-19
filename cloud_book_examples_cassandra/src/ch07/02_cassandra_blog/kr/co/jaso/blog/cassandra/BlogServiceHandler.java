package kr.co.jaso.blog.cassandra;

import java.util.ArrayList;
import java.util.List;

import kr.co.jaso.blog.cassandra.dao.BlogDAO;
import kr.co.jaso.blog.cassandra.dao.BlogSearchDAO;
import kr.co.jaso.blog.cassandra.generated.BlogArticle;
import kr.co.jaso.blog.cassandra.generated.BlogException;
import kr.co.jaso.blog.cassandra.generated.BlogService;
import kr.co.jaso.blog.cassandra.generated.ServiceStatus;

import org.apache.thrift.TException;

public class BlogServiceHandler implements BlogService.Iface {
  private BlogServer server;
  private BlogDAO blogDAO;
  private BlogSearchDAO blogSearchDAO;
  
  public BlogServiceHandler(BlogServer server, BlogDAO blogDAO, BlogSearchDAO blogSearchDAO) throws Exception {
    this.server = server;
    this.blogDAO = blogDAO;
    this.blogSearchDAO = blogSearchDAO;
  }
  
  @Override
  public BlogArticle getBlog(String userId, long articleId) throws BlogException, TException {
    try {
      return blogDAO.getBlog(userId + articleId);
    } catch (Exception e) {
      e.printStackTrace();
      throw new BlogException(server.getHostName(), e.getMessage());
    }
  }

  @Override
  public void saveBlog(BlogArticle article) throws TException {
    try {
      article.setArticleId(System.currentTimeMillis() + System.nanoTime());
      blogDAO.saveBlog(article);
      blogSearchDAO.saveBlog(article);
    } catch (Exception e) {
      e.printStackTrace();
      throw new TException(e.getMessage());
    }
  }

  @Override
  public List<BlogArticle> searchBlogByUserId(String userId) throws BlogException, TException {
    try {
      return blogDAO.searchBlogByUserId(userId);
    } catch (Exception e) {
      e.printStackTrace();
      throw new BlogException(server.getHostName(), e.getMessage());
    }  
  }

  @Override
  public List<BlogArticle> searchBlogByKeyword(String userId, String keyword) throws BlogException, TException {
    try {
      List<String> ids = blogSearchDAO.searchBlogByKeywork(userId, keyword);
      
      List<BlogArticle> articles = new ArrayList<BlogArticle>();
      for(String eachId: ids) {
        BlogArticle blogArticle = blogDAO.getBlog(eachId);
        if(blogArticle != null) {
          articles.add(blogArticle);
        }
      }
      
      return articles;
    } catch (Exception e) {
      e.printStackTrace();
      throw new BlogException(server.getHostName(), e.getMessage());
    }  
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
