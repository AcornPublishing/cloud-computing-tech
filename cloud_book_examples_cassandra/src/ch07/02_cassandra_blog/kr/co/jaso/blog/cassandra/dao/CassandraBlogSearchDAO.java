package kr.co.jaso.blog.cassandra.dao;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.jaso.blog.cassandra.generated.BlogArticle;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class CassandraBlogSearchDAO implements BlogSearchDAO {

  @Override
  public void saveBlog(BlogArticle article) throws Exception {
    TTransport tr = new TSocket("localhost", 9160);
    TProtocol proto = new TBinaryProtocol(tr);
    Cassandra.Client client = new Cassandra.Client(proto);
    tr.open();
    try {
      String keyspace = "BlogSearch";
      String columnFamily = "BlogTermIndex";
  
      long timestamp = System.currentTimeMillis();
  
      List<Term> terms = parseTerm(article);
      
      for(Term eachTerm: terms) {
        ColumnPath colPathName = new ColumnPath(columnFamily);
        colPathName.setSuper_column(ByteBuffer.wrap(eachTerm.getTerm().getBytes()));
        colPathName.setColumn(ByteBuffer.wrap((article.getUserId() + article.getArticleId()).getBytes()));
        client.insert(keyspace, article.getUserId(), colPathName, 
            ByteBuffer.wrap(("" + eachTerm.getTermFrequence()).getBytes()), timestamp, ConsistencyLevel.ONE);
      }
    } finally {
      tr.close();
    }
  }

  @Override
  public List<String> searchBlogByKeywork(String userId, String keyword) throws Exception {
    TTransport tr = new TSocket("localhost", 9160);
    TProtocol proto = new TBinaryProtocol(tr);
    Cassandra.Client client = new Cassandra.Client(proto);
    tr.open();
    try {
      String keyspace = "BlogSearch";
      String columnFamily = "BlogTermIndex";
      
      ColumnParent parent = new ColumnParent(columnFamily);
      parent.setSuper_column(ByteBuffer.wrap(keyword.getBytes()));
      SlicePredicate predicate = new SlicePredicate();
      SliceRange sliceRange = new SliceRange();
      sliceRange.setStart(ByteBuffer.wrap(new byte[0]));
      sliceRange.setFinish(ByteBuffer.wrap(new byte[0]));
      predicate.setSlice_range(sliceRange);
      
      List<ColumnOrSuperColumn> results = client.get_slice(keyspace, userId, 
          parent, predicate, ConsistencyLevel.ONE);

      List<Object[]> searchResult = new ArrayList<Object[]>();
      for (ColumnOrSuperColumn result : results) {
        Column column = result.column;
        String articleRowKey = new String(column.name.array());
        int frequence = Integer.parseInt(new String(column.value.array()));
        searchResult.add(new Object[]{articleRowKey, frequence});
      }
      
      Collections.sort(searchResult, new Comparator<Object[]>() {
        @Override
        public int compare(Object[] o1, Object[] o2) {
          if((Integer)o1[1] < (Integer)o2[1]) {
            return 1;
          } else if((Integer)o1[1] > (Integer)o2[1]) {
            return -1;
          } else {
            return 0;
          }
        }
      });
      
      List<String> result = new ArrayList<String>();
      for(Object[] eachResult: searchResult) {
        result.add((String)eachResult[0]);
      }
      
      return result;
    } finally {
      tr.close();
    }
  }
  
  private List<Term> parseTerm(BlogArticle article) {
    StringBuilder sb = new StringBuilder();
    
    sb.append(article.getTitle()).append(" ");
    sb.append(new String(article.getContents().array()));
    
    String[] tokens = sb.toString().split(" ");
    Map<String, Integer> tokenMap = new HashMap<String, Integer>();
    for(String eachToken: tokens) {
      if(tokenMap.containsKey(eachToken)) {
        tokenMap.put(eachToken, tokenMap.get(eachToken) + 1);
      } else {
        tokenMap.put(eachToken, 1);
      }
    }
    
    List<Term> terms = new ArrayList<Term>();
    
    for(Map.Entry<String, Integer> entry: tokenMap.entrySet()) {
      terms.add(new Term(entry.getKey(), entry.getValue()));
    }
    return terms;
  }
  
  class Term {
    String term;
    int termFrequence;
    
    public Term(String term, int termFrequence) {
      this.term = term;
      this.termFrequence = termFrequence;
    }
    public String getTerm() {
      return term;
    }
    public int getTermFrequence() {
      return termFrequence;
    }
  }
}
