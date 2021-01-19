package kr.co.jaso.hadoop.wordcount;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.StringUtils;

public class ComplexWordCountInputFormat extends InputFormat<LongWritable, Text> {
  private static final Log LOG = LogFactory.getLog(ComplexWordCountInputFormat.class);
  
  @Override
  public RecordReader<LongWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context)
      throws IOException, InterruptedException {
    return new ComplexWordCountLineRecordReader();
  }

  @Override
  public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
    Configuration conf = context.getConfiguration();
    String inputDirs = conf.get("mapred.input.dir", "");
    String [] list = StringUtils.split(inputDirs);
    Path[] inputPaths = new Path[list.length];
    for (int i = 0; i < list.length; i++) {
      inputPaths[i] = new Path(StringUtils.unEscapeString(list[i]));
    }
    
    FileSystem fs = FileSystem.get(context.getConfiguration());
    List<InputSplit> inputSplits = new ArrayList<InputSplit>(list.length); 

    for(Path eachPath: inputPaths) {
      FileStatus[] files = fs.listStatus(eachPath);
      for(FileStatus eachFile: files) {
        long length = eachFile.getLen();
        if(length == 0) {
          continue;
        }
        long blockSize = eachFile.getBlockSize();
        long blockStartPos = 0;
        long remainBytes = length;
        while(remainBytes > 0) {
          long currentBlockLength = (remainBytes > blockSize ? blockSize : remainBytes);
          BlockLocation[] blockLocations = fs.getFileBlockLocations(eachFile, blockStartPos, currentBlockLength);
          for(BlockLocation eachBlock: blockLocations) {
            FileSplit fileSplit = new FileSplit(eachFile.getPath(), blockStartPos, currentBlockLength, eachBlock.getHosts());
            inputSplits.add(fileSplit);
          }
          remainBytes = remainBytes - blockSize;
          blockStartPos = blockStartPos + blockSize; 
        }
      }
    }
    
    return inputSplits;
  }
}
