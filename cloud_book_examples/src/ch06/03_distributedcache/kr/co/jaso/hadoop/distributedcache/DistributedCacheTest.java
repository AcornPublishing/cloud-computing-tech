package kr.co.jaso.hadoop.distributedcache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class DistributedCacheTest {
  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    if (args.length != 3) {
      System.err.println("Usage: java DistributedCacheTest <in> <out> <code file(hdfs)>");
      System.exit(1);
    }
    Job job = new Job(conf, "word count");
    job.setJarByClass(DistributedCacheTest.class);
    job.setMapperClass(CodeCompareMapper.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    
    job.setInputFormatClass(TextInputFormat.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));

    job.setNumReduceTasks(0);
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    
    DistributedCache.addCacheFile(new URI(args[2]), job.getConfiguration());
    job.waitForCompletion(true);
  }
  
  static class CodeCompareMapper extends Mapper<LongWritable, Text, Text, Text>{
    private final static Text empty = new Text("");
    private Set<String> codes = new HashSet<String>();
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
      if(codes.contains(value.toString())) {
        context.write(value, empty);
      }
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      super.setup(context);

      URI[] codeFiles = DistributedCache.getCacheFiles(context.getConfiguration());
      
      Path path = new Path(codeFiles[0].toString());
      FileSystem fs = path.getFileSystem(context.getConfiguration());
      
      BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(path)));
      
      String line = null;
      
      while( (line = reader.readLine()) != null ) {
        codes.add(line);
      }
      reader.close();
    }
  }
}
