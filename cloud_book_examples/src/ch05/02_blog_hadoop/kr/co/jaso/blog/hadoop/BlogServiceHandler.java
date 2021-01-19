package kr.co.jaso.blog.hadoop;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import kr.co.jaso.blog.hadoop.generated.BlogService;
import kr.co.jaso.blog.hadoop.generated.BlogArticle;
import kr.co.jaso.blog.hadoop.generated.BlogException;
import kr.co.jaso.blog.hadoop.generated.ServiceStatus;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.thrift.TException;

public class BlogServiceHandler implements BlogService.Iface {
  public static final String BLOG_ROOT = "/user/blog/article";
  
  private BlogServer server;
  private FileSystem fs;
  
  public BlogServiceHandler(BlogServer server, String hdfsUri) throws Exception {
    this.server = server;
    Configuration conf = new Configuration();
    conf.set("fs.default.name", hdfsUri);
    this.fs = FileSystem.get(conf);
    if(!fs.exists(new Path(BLOG_ROOT))) {
      fs.mkdirs(new Path(BLOG_ROOT));
    }
  }
  
  @Override
  public BlogArticle getBlog(String userId, long articleId) throws BlogException, TException {
    Path articlePath = new Path(BLOG_ROOT + "/" + userId + "/" + articleId);
    
    try {
      DataInputStream in = fs.open(articlePath);
      
      return readArticle(in);
    } catch (Exception e) {
      e.printStackTrace(); 
      throw new BlogException(server.getHostName(), e.getMessage());
    }
  }

  @Override
  public void saveBlog(BlogArticle article) throws TException {
    article.setArticleId(System.currentTimeMillis() + System.nanoTime());
    Path blogPath = new Path(BLOG_ROOT + "/" + article.getUserId());
    try {
      if(!fs.exists(blogPath)) {
        fs.mkdirs(blogPath);
      } 
      
      DataOutputStream dout = fs.create(new Path(blogPath, String.valueOf(article.getArticleId())));
      try {
        writeArticle(article, dout);
      } finally {
        dout.close();
      }
    } catch(IOException e) {
      e.printStackTrace();
      throw new TException(e.getMessage());
    }
  }

  @Override
  public List<BlogArticle> searchBlogByUserId(String userId) throws BlogException, TException {
    List<BlogArticle> result = new ArrayList<BlogArticle>();
    
    try {
      FileStatus[] files = fs.listStatus(new Path(BLOG_ROOT + "/" + userId));
      for(FileStatus eachFile: files) {
        result.add(getBlog(userId, Long.parseLong(eachFile.getPath().getName())));
      }
    } catch (Exception e) {
      e.printStackTrace(); 
      throw new BlogException(server.getHostName(), e.getMessage());
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
  
  public static void writeArticle(BlogArticle article, DataOutput out) throws IOException {
    out.writeInt(article.getUserId().getBytes().length);
    out.write(article.getUserId().getBytes());
    
    out.writeLong(article.getArticleId());
    
    out.writeInt(article.getTitle().getBytes().length);
    out.write(article.getTitle().getBytes());
    
    out.writeInt(article.getContents().length);
    out.write(article.getContents());
  }
  
  public static BlogArticle readArticle(DataInput in) throws IOException {
    BlogArticle article = new BlogArticle();

    article.setUserId(readString(in));
    article.setArticleId(in.readLong());
    article.setTitle(readString(in));
    article.setContents(ByteBuffer.wrap(readBytes(in)));
    
    return article;
  }
  
  private static String readString(DataInput in) throws IOException {
    byte[] buf = new byte[in.readInt()];
    
    in.readFully(buf);
    
    return new String(buf, 0, buf.length);
  }

  private static byte[] readBytes(DataInput in) throws IOException {
    byte[] buf = new byte[in.readInt()];
    
    in.readFully(buf);
    
    return buf;
  }
}
