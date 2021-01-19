package kr.co.jaso.hadoop.wordcount;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskID;

public class ComplexWordCountRecordWriter extends RecordWriter<Text, IntWritable> {
  private OutputStream out;
  private byte[] keyValueSeperator;
  
  public ComplexWordCountRecordWriter(Path outputPath, TaskAttemptContext context) throws IOException {
    keyValueSeperator = "\t".getBytes("UTF-8");
    
    TaskID taskId = context.getTaskAttemptID().getTaskID();
    
    Path taskOutputFile = new Path(outputPath, "wordcount-result-" + taskId.getId());
    
    FileSystem fs = taskOutputFile.getFileSystem(context.getConfiguration());
    out = fs.create(taskOutputFile);
  }

  @Override
  public void close(TaskAttemptContext context) throws IOException, InterruptedException {
    if(out != null) {
      out.close();
    }
  }

  @Override
  public void write(Text key, IntWritable value) throws IOException, InterruptedException {
    out.write(key.getBytes(), 0, key.getLength());
    out.write(keyValueSeperator);
    out.write(value.toString().getBytes());
    out.write("\n".getBytes());
  }
}
