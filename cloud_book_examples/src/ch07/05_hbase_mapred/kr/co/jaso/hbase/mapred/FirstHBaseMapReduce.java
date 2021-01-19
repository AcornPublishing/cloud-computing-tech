package kr.co.jaso.hbase.mapred;

import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;

public class FirstHBaseMapReduce {
  static final String TABLE_NAME = "SampleTable";
  static final String COLUMN_FAMILY = "ColumnFamily1";

  static final String OUTPUT_TABLE_NAME = "InvertedTable";
  static final String OUTPUT_COLUMN_FAMILY = "InvertedCloumn";

  public static void main(String[] args) throws Exception {
    //0.21에서는 HBaseConfiguration conf = new HBaseConfiguration();
    Configuration conf = HBaseConfiguration.create();

    // 테스트 데이터 생성
    putTestDatas(conf);

    // Output 테이블 생성
    HBaseAdmin admin = new HBaseAdmin(conf);
    HTableDescriptor outputTableDesc = new HTableDescriptor(OUTPUT_TABLE_NAME);
    outputTableDesc.addFamily(new HColumnDescriptor(Bytes.toBytes(OUTPUT_COLUMN_FAMILY)));
    admin.disableTable(OUTPUT_TABLE_NAME);
    admin.deleteTable(OUTPUT_TABLE_NAME);
    if (!admin.tableExists(OUTPUT_TABLE_NAME)) {
      admin.createTable(outputTableDesc);
    }

    Job job = new Job(conf, TABLE_NAME);
    job.setJarByClass(FirstHBaseMapReduce.class);

    // Mapper 설정
    Scan scan = new Scan();
    scan.addFamily(COLUMN_FAMILY.getBytes());

    // (1)번 항목
    TableMapReduceUtil.initTableMapperJob(TABLE_NAME, scan, FirstHBaseMapper.class, ImmutableBytesWritable.class,
        ImmutableBytesWritable.class, job);

    // Reduce 설정
    // (2)번 항목
    TableMapReduceUtil.initTableReducerJob(OUTPUT_TABLE_NAME, FirstHBaseReducer.class, job);

    // Map갯수를 Reduce 갯수와 동일하게 한다.
    HTable htable = new HTable(conf, TABLE_NAME);
    byte[][] startKeys = htable.getStartKeys();
    job.setNumReduceTasks(startKeys.length);

    // 작업 시작
    job.waitForCompletion(true);

    // 결과 출력
    printInvertedTable(conf);
  }

  static void putTestDatas(Configuration conf) throws Exception {
    HBaseAdmin admin = new HBaseAdmin(conf);
    if (admin.tableExists(TABLE_NAME)) {
      System.out.println("Table " + TABLE_NAME + " already exists.");
      return;
    }
    HTableDescriptor testTableDesc = new HTableDescriptor(TABLE_NAME);
    testTableDesc.addFamily(new HColumnDescriptor(Bytes.toBytes(COLUMN_FAMILY)));
    admin.createTable(testTableDesc);
    HTable table = new HTable(conf, TABLE_NAME);
    
    for (int i = 0; i < 10000; i++) {
      String rowKey = "row" + i;
      Put put = new Put(Bytes.toBytes(rowKey));
      put.add(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("CF_key_" + (i+1)), Bytes.toBytes((i + 1) + "_Value"));
      table.put(put);
    }
  }
  
  static void printInvertedTable(Configuration conf) throws Exception {
    HTable table = new HTable(conf, TABLE_NAME);
    Scan scan = new Scan();
    scan.addColumn(Bytes.toBytes(COLUMN_FAMILY));
    ResultScanner scanner = table.getScanner(scan);
    
    Result result = null;
    try {
      while ((result = scanner.next()) != null) {
        System.out.println("scanned row: " + result);
      }
    } finally {
      scanner.close();
    }
  }
  
  // (3)번 항목
  static class FirstHBaseMapper extends TableMapper<ImmutableBytesWritable, ImmutableBytesWritable> {
    @Override
    protected void map(ImmutableBytesWritable row, Result value, Context context) throws IOException,
        InterruptedException {
      NavigableMap<byte[], byte[]> colDatas = value.getFamilyMap(Bytes.toBytes(COLUMN_FAMILY));

      for (Map.Entry<byte[], byte[]> entry : colDatas.entrySet()) {
        ImmutableBytesWritable invertedKey = new ImmutableBytesWritable(entry.getValue());
        try {
          context.write(invertedKey, row);
        } catch (InterruptedException e) {
          throw new IOException(e);
        }
      }
    }
  }

  // (4)번 항목
  static class FirstHBaseReducer extends
      TableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {
    public void reduce(ImmutableBytesWritable key, Iterable<ImmutableBytesWritable> values, Context context)
        throws IOException, InterruptedException {
      Put put = new Put(key.get());
      for (ImmutableBytesWritable val : values) {
        put.add(Bytes.toBytes(OUTPUT_COLUMN_FAMILY), val.get(), null);
      }
      // (5)번 항목
      context.write(key, put);
    }
  }
}