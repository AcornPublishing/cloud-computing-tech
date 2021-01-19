package kr.co.jaso.blog.cassandra.dao;

import java.util.List;

import kr.co.jaso.blog.cassandra.generated.BlogArticle;

public interface BlogSearchDAO {
  public void saveBlog(BlogArticle article) throws Exception;
  public List<String> searchBlogByKeywork(String userId, String keyword) throws Exception;
}
