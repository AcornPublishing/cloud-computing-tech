package kr.co.jaso.blog.cassandra.dao;

import java.util.List;

import kr.co.jaso.blog.cassandra.generated.BlogArticle;

public interface BlogDAO {
  public BlogArticle getBlog(String string) throws Exception;
  public void saveBlog(BlogArticle article) throws Exception;
  public List<BlogArticle> searchBlogByUserId(String userId) throws Exception;
}
