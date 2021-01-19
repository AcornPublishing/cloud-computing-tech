package kr.co.jaso.hadoop.wordcount;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.lib.InputSampler;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ComplexWordCountDriver {
  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    if (args.length != 2) {
      System.err.println("Usage: java ComplexWordCountDriver <in> <out>");
      System.exit(2);
    }
    Job job = new Job(conf, "word count");
    job.setJarByClass(ComplexWordCountDriver.class);
    job.setMapperClass(ComplexWordCountMapper.class);
    job.setMapOutputKeyClass(Text.class);
    
    job.setInputFormatClass(ComplexWordCountInputFormat.class);
    job.setPartitionerClass(ComplexWordCountPartitioner.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));

    job.setReducerClass(ComplexWordCountReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    job.setOutputFormatClass(ComplexWordCountOutputFormat.class);
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    job.waitForCompletion(true);
  }
}
