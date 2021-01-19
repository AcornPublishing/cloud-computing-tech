package kr.co.jaso.blog.cloudata;

import java.util.List;

import kr.co.jaso.blog.cloudata.dao.BlogDAO;
import kr.co.jaso.blog.cloudata.generated.BlogArticle;
import kr.co.jaso.blog.cloudata.generated.BlogException;
import kr.co.jaso.blog.cloudata.generated.BlogService;
import kr.co.jaso.blog.cloudata.generated.ServiceStatus;

import org.apache.thrift.TException;

public class BlogServiceHandler implements BlogService.Iface {
  private BlogServer server;
  private BlogDAO blogDAO;
  
  public BlogServiceHandler(BlogServer server, BlogDAO blogDAO) throws Exception {
    this.server = server;
    this.blogDAO = blogDAO;
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
  public ServiceStatus getServiceStatus() throws TException {
    ServiceStatus serviceStatus = new ServiceStatus();
    
    serviceStatus.setHostName(server.getHostName());
    serviceStatus.setPort(server.getPort());
    serviceStatus.setStatus("OK");
    
    return serviceStatus;
  }
}
