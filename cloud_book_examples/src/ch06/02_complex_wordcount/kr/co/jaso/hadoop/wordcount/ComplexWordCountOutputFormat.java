package kr.co.jaso.hadoop.wordcount;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InvalidJobConfException;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class ComplexWordCountOutputFormat extends OutputFormat<Text, IntWritable> {
  @Override
  public void checkOutputSpecs(JobContext context) throws IOException, InterruptedException {
    String outputPathName = context.getConfiguration().get("mapred.output.dir");
    if(outputPathName == null) {
      throw new InvalidJobConfException("Output directory not set.");
    }
    
    Path outputDir = new Path(outputPathName);
    if (outputDir.getFileSystem(context.getConfiguration()).exists(outputDir)) {
      throw new InvalidJobConfException("Output directory " + outputPathName + " already exists.");
    }
  }

  @Override
  public OutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException, InterruptedException {
    return new ComplexWordCountOutputCommitter();
  }

  @Override
  public RecordWriter<Text, IntWritable> getRecordWriter(TaskAttemptContext context) throws IOException,
      InterruptedException {
    return new ComplexWordCountRecordWriter(getOutputPath(context), context);
  }
  
  private Path getOutputPath(TaskAttemptContext context) {
    String name = context.getConfiguration().get("mapred.output.dir");
    Path outputPath = (name == null ? null: new Path(name));
    return outputPath;
  }
}
