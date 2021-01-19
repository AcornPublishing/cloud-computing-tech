package kr.co.jaso.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoDBTest {
  public static void main(String[] args) throws Exception {
    Mongo mongo = new Mongo("127.0.0.1", 10000);
    DB testDB = mongo.getDB("mydb");

    DBCollection blogCollect = testDB.getCollection("blog");

    BasicDBObject blogPost = new BasicDBObject();
    blogPost.put("title", "Cloud Computing");
    blogPost.put("author", "김형준");

    blogCollect.insert(blogPost);

    blogPost = new BasicDBObject();
    blogPost.put("title", "클라우드 컴퓨팅");
    blogPost.put("author", "김형준");

    blogCollect.insert(blogPost);

    BasicDBObject query = new BasicDBObject();
    query.put("title", "Cloud Computing");

    DBObject result = blogCollect.findOne(query);
    System.out.println(result);
    System.out.println("==============================");

    DBCursor cursor = blogCollect.find();

    while (cursor.hasNext()) {
      System.out.println(cursor.next());
    }
    System.out.println("==============================");

    blogCollect.remove(result);
    result = blogCollect.findOne(query);
    if (result == null) {
      System.out.println("No data");
    } else {
      System.out.println(result);
    }
  }
}
