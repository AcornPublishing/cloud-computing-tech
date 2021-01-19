package kr.co.jaso.hbase.basic;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

public class HBaseTest {
  public static void main(String[] args) throws Exception {
    String tableName = "T_TEST";
    String columnFamily = "MyColumnFamily";

    //0.21에서는 HBaseConfiguration config = new HBaseConfiguration();
    Configuration config = HBaseConfiguration.create();

    HBaseAdmin admin = new HBaseAdmin(config);
    HTableDescriptor testTableDesc = new HTableDescriptor(tableName);
    testTableDesc.addFamily(new HColumnDescriptor(Bytes.toBytes(columnFamily)));
    if (!admin.tableExists(tableName)) {
      admin.createTable(testTableDesc);
    }

    HTable table = new HTable(config, tableName);

    for (int i = 0; i < 10; i++) {
      String rowKey = "row" + i;
      Put put = new Put(Bytes.toBytes(rowKey));
      put.add(Bytes.toBytes(columnFamily), Bytes.toBytes("MyColName1"), Bytes.toBytes("MyCol1_" + i + "_Value"));
      put.add(Bytes.toBytes(columnFamily), Bytes.toBytes("MyColName2"), Bytes.toBytes("MyCol2_" + i + "_Value"));
      table.put(put);
    }

    Get get = new Get(Bytes.toBytes("row1"));
    Result result = table.get(get);
    byte[] value = result.getValue(Bytes.toBytes(columnFamily), Bytes.toBytes("MyColName1"));
    System.out.println("get result: " + Bytes.toString(value));

    Scan scan = new Scan();
    scan.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes("MyColName1"));
    ResultScanner scanner = table.getScanner(scan);
    try {
      while ((result = scanner.next()) != null) {
        System.out.println("scanned row: " + result);
      }
    } finally {
      scanner.close();
    }
  }

}
