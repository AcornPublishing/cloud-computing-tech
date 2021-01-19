package kr.co.jaso.blog.cloudata.dao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.jaso.blog.cloudata.generated.BlogArticle;
import kr.co.jaso.blog.cloudata.generated.BlogReply;

import org.cloudata.core.client.CTable;
import org.cloudata.core.client.Cell;
import org.cloudata.core.client.Row;
import org.cloudata.core.client.RowFilter;
import org.cloudata.core.common.conf.CloudataConf;
import org.cloudata.core.tablet.TableSchema;

public class CloudataBlogDAO implements BlogDAO {
  private static final String TABLE_NAME = "T_BLOG";
  
  private CloudataConf conf;
  public CloudataBlogDAO() throws Exception {
    conf = new CloudataConf(); 
    if(CTable.existsTable(conf, TABLE_NAME)) {
      return;
    }
    TableSchema tableSchema = new TableSchema(TABLE_NAME);
    tableSchema.addColumn("ArticleInfo");
    tableSchema.addColumn("Contents");
    tableSchema.addColumn("Reply");
    
    CTable.createTable(conf, tableSchema);
    System.out.println(TABLE_NAME + " created");
  }
  
  @Override
  public BlogArticle getBlog(String key) throws Exception {
    CTable ctable = CTable.openTable(conf, TABLE_NAME);
    Row row = ctable.get(new Row.Key(key));
    return getBlogArticleFromRow(row);
  }

  private BlogArticle getBlogArticleFromRow(Row row) throws Exception {
    if(row == null) {
      return null;
    }
    
    BlogArticle blogArticle = new BlogArticle();
    blogArticle.setContents(ByteBuffer.wrap(row.getFirst("Contents").getBytes()));
    
    Map<Cell.Key, Cell> infoCells = row.getCellMap("ArticleInfo");
    blogArticle.setUserId(new String(infoCells.get(new Cell.Key("userId")).getBytes()));
    blogArticle.setArticleId(Long.parseLong(new String(infoCells.get(new Cell.Key("articleId")).getBytes())));
    blogArticle.setTitle(new String(infoCells.get(new Cell.Key("title")).getBytes()));
    
    Map<Cell.Key, Cell> replyCells = row.getCellMap("Reply");
    List<BlogReply> replies = new ArrayList<BlogReply>();
    if(replyCells != null) {
      for(Map.Entry<Cell.Key, Cell> entry: replyCells.entrySet()) {
        replies.add(getBlogReply(entry.getValue().getBytes()));
      }
      blogArticle.setReplies(replies);
    }
    return blogArticle;
  }

  @Override
  public void saveBlog(BlogArticle article) throws Exception {
    CTable ctable = CTable.openTable(conf, TABLE_NAME);

    Row row = new Row(new Row.Key(article.getUserId() + article.getArticleId()));
    row.addCell("Contents", new Cell(Cell.Key.EMPTY_KEY, article.getContents()));
    
    row.addCell("ArticleInfo", new Cell(new Cell.Key("userId"), article.getUserId().getBytes()));
    row.addCell("ArticleInfo", new Cell(new Cell.Key("articleId"), ("" + article.getArticleId()).getBytes()));
    row.addCell("ArticleInfo", new Cell(new Cell.Key("title"), article.getTitle().getBytes()));
    
    List<BlogReply> replies = article.getReplies();
    if(replies != null) {
      long currentKey = System.currentTimeMillis();
      for(BlogReply eachReply: replies) {
        row.addCell("Reply", new Cell(
            new Cell.Key(("" + (currentKey++)).getBytes()), 
            getReplyBytes(eachReply)));
      }
    }
    ctable.put(row);
  }

  @Override
  public List<BlogArticle> searchBlogByUserId(String userId) throws Exception {
    CTable ctable = CTable.openTable(conf, TABLE_NAME);
    
    RowFilter rowFilter = new RowFilter();
    rowFilter.addAllCellFilter(ctable.descTable());
    rowFilter.setValue(new Row.Key(userId), new Row.Key(userId + "a"), RowFilter.OP_BETWEEN);
    Row[] rows = ctable.gets(rowFilter);
    if(rows == null) {
      return new ArrayList<BlogArticle>();
    }
    
    List<BlogArticle> articles = new ArrayList<BlogArticle>();
    for(Row eachRow: rows) {
      BlogArticle article = getBlogArticleFromRow(eachRow);
      articles.add(article);
    }
    
    return articles;
  }

  private BlogReply getBlogReply(byte[] bytes) throws Exception {
    BlogReply blogReply = new BlogReply();
    DataInputStream din = new DataInputStream(new ByteArrayInputStream(bytes));
    
    byte[] buf = new byte[din.read()];
    din.readFully(buf);
    blogReply.setUserName(new String(buf));
    
    buf = new byte[din.read()];
    din.readFully(buf);
    blogReply.setReplyContent(ByteBuffer.wrap(buf));
    
    return blogReply;
  }
  
  private byte[] getReplyBytes(BlogReply blogReply) throws Exception {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    
    bout.write(blogReply.getUserName().getBytes().length);
    bout.write(blogReply.getUserName().getBytes());
    bout.write(blogReply.getReplyContent().length);
    bout.write(blogReply.getReplyContent());
    
    return bout.toByteArray();
  }
}
