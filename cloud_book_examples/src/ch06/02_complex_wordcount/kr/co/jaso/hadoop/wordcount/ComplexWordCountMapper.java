package kr.co.jaso.hadoop.wordcount;

import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class ComplexWordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
  private final static IntWritable one = new IntWritable(1);

  public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
    StringTokenizer itr = new StringTokenizer(value.toString());
    while (itr.hasMoreTokens()) {
      context.write(new Text(itr.nextToken()), one);
    }
  }
  
  public static void main(String[] args) throws Exception {
    System.out.println(new Date(1284737325887L));
  }
}
