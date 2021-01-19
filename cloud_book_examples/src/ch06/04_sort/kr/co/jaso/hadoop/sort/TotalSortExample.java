package kr.co.jaso.hadoop.sort;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.InputSampler;
import org.apache.hadoop.mapred.lib.TotalOrderPartitioner;


public class TotalSortExample {
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: java TotalSortExample <in> <out>");
      System.exit(2);
    }
    
    JobConf job = new JobConf(TotalSortExample.class);
    JobClient jobClient = new JobClient(job);

    job.setJobName("total sort");

    job.setMapOutputKeyClass(LongWritable.class);
    job.setMapOutputValueClass(Text.class);
    
    job.setInputFormat(SequenceFileInputFormat.class);
    job.setPartitionerClass(TotalOrderPartitioner.class);
    TotalOrderPartitioner.setPartitionFile(job, new Path(args[0], "_partition"));
    FileInputFormat.addInputPath(job, new Path(args[0]));

    job.setOutputKeyClass(LongWritable.class);
    job.setOutputValueClass(Text.class);
    job.setOutputFormat(TextOutputFormat.class);
    job.setNumReduceTasks(jobClient.getClusterStatus().getMaxReduceTasks());
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

     InputSampler.Sampler<LongWritable, Text> sampler = 
      new InputSampler.RandomSampler<LongWritable, Text>(0.1, 10000, 100);

    InputSampler.writePartitionFile(job, sampler);

    JobClient.runJob(job);
    
  }
}

