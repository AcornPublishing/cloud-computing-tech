package kr.co.jaso.hadoop.sort;

import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.CompressionType;

public class GenTestData {
  public static void main(String[] args) throws Exception {
    if(args.length < 2) {
      System.out.println("java GenTestData <num record> <output path>");
      return;
    }
    
    FileSystem fs = FileSystem.get(new Configuration());
    
    Random rand = new Random(System.currentTimeMillis());

    SequenceFile.Writer out = 
      SequenceFile.createWriter(fs, fs.getConf(), new Path(args[1]),
                                LongWritable.class,
                                Text.class,
                                CompressionType.NONE,
                                null,
                                null);
    
    try {
      int numRecord = Integer.parseInt(args[0]);
      for(int i = 0; i < numRecord; i++) {
        long key = rand.nextInt(numRecord);
        out.append(new LongWritable(key), new Text("\t this is test value:" + key));
      }
    } finally {
      out.close();
    }
  }
}
