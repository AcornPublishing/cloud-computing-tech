package kr.co.jaso.cassandra.basic;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.KeyRange;
import org.apache.cassandra.thrift.KeySlice;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class CassandraExample {
  public static void main(String[] args) throws  Exception {
    //thrift 접속 처리
    TTransport tr = new TSocket("localhost", 9160);
    TProtocol proto = new TBinaryProtocol(tr);
    Cassandra.Client client = new Cassandra.Client(proto);
    tr.open();

    String keyspace = "Keyspace1";
    String columnFamily = "Standard1";
    String keyUserID = "3";

    //데이터 입력
    long timestamp = System.currentTimeMillis();

    ColumnPath colPathName = new ColumnPath(columnFamily);
    colPathName.setColumn(ByteBuffer.wrap("fullName".getBytes()));

    client.insert(keyspace, keyUserID, colPathName, ByteBuffer.wrap("babokim".getBytes()), timestamp, ConsistencyLevel.ONE);

    ColumnPath colPathAge = new ColumnPath(columnFamily);
    colPathAge.setColumn(ByteBuffer.wrap("age".getBytes()));

    client.insert(keyspace, keyUserID, colPathAge, ByteBuffer.wrap("24".getBytes()), timestamp, ConsistencyLevel.ONE);

    //데이터 조회: 컬럼 1개
    System.out.println("single column:");
    Column col = client.get(keyspace, keyUserID, colPathName, ConsistencyLevel.ONE).getColumn();

    System.out.println("column name: " + new String(col.name.array()));
    System.out.println("column value: " + new String(col.value.array()));
    System.out.println("column timestamp: " + new Date(col.timestamp));

    //데이터 조회: 하나의 row 전체 컬럼 
    SlicePredicate predicate = new SlicePredicate();
    SliceRange sliceRange = new SliceRange();
    sliceRange.setStart(ByteBuffer.wrap(new byte[0]));
    sliceRange.setFinish(ByteBuffer.wrap(new byte[0]));
    predicate.setSlice_range(sliceRange);

    System.out.println("\nrow:");
    ColumnParent parent = new ColumnParent(columnFamily);
    List<ColumnOrSuperColumn> results = client.get_slice(keyspace, keyUserID, parent, predicate, ConsistencyLevel.ONE);
    for (ColumnOrSuperColumn result : results) {
      Column column = result.column;
      System.out.println(new String(column.name.array()) + ": " + new String(column.value.array()));
    }
    
    //데이터 조회: 여러개의 row 조회
    KeyRange keyRange = new KeyRange();
    keyRange.setStart_token("1");
    keyRange.setEnd_token("1");
    
    List<KeySlice> results2 = client.get_range_slices(keyspace, parent, predicate, keyRange, ConsistencyLevel.ONE);
    
    System.out.println("\nkey range:");
    for (KeySlice eachResult : results2) {
      System.out.println("====" + eachResult.getKey() + "=====");
      List<ColumnOrSuperColumn> columns = eachResult.getColumns();
      for (ColumnOrSuperColumn result : columns) {
        Column column = result.column;
        System.out.println(new String(column.name.array()) + ": " + new String(column.value.array()));
      }
    }
    
    //thrift 접속 종료
    tr.close();
  }
}