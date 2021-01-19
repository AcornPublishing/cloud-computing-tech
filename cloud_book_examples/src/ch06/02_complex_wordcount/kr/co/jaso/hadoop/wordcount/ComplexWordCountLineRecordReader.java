package kr.co.jaso.hadoop.wordcount;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class ComplexWordCountLineRecordReader extends RecordReader<LongWritable, Text> {
  private static final Log LOG = LogFactory.getLog(ComplexWordCountLineRecordReader.class);

  private long start;
  private long pos;
  private long end;
  private BufferedReader reader;
  private LongWritable key = null;
  private Text value = null;

  @Override
  public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException, InterruptedException {
    FileSplit split = (FileSplit) genericSplit;
    Configuration job = context.getConfiguration();
    this.start = split.getStart();
    this.end = this.start + split.getLength();
    final Path file = split.getPath();
    FileSystem fs = file.getFileSystem(job);
    this.reader = new BufferedReader(new InputStreamReader(fs.open(file)));
    this.pos = this.start;
    context.setStatus(file + " (" + start + " ~ " + end + ")");
  }

  @Override
  public LongWritable getCurrentKey() throws IOException, InterruptedException {
    return key;
  }

  @Override
  public Text getCurrentValue() throws IOException, InterruptedException {
    return value;
  }

  @Override
  public float getProgress() throws IOException, InterruptedException {
    if (start == end) {
      return 0.0f;
    } else {
      return Math.min(1.0f, (pos - start) / (float)(end - start));
    }
  }

  @Override
  public boolean nextKeyValue() throws IOException, InterruptedException {
    if (key == null) {
      key = new LongWritable();
    }
    key.set(pos);
    
    if (value == null) {
      value = new Text();
    }
    
    String line = reader.readLine();
    if(line == null) {
      key = null;
      value = null;
      return false;
    }
    pos += line.getBytes().length;
    if(pos >= end) {
      key = null;
      value = null;
      return false;
    }
    value.set(line.getBytes("utf-8"));
    
    return true;
  }

  @Override
  public void close() throws IOException {
    if(reader != null) {
      reader.close();
    }
  }
}
