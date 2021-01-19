package kr.co.jaso.hadoop.wordcount;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class ComplexWordCountOutputCommitter extends OutputCommitter {
  static final Log LOG = LogFactory.getLog(ComplexWordCountOutputCommitter.class);
  
  @Override
  public void abortTask(TaskAttemptContext taskContext) throws IOException {
    LOG.info("ComplexWordCountOutputCommitter:abortTask");
  }

  @Override
  public void cleanupJob(JobContext jobContext) throws IOException {
    LOG.info("ComplexWordCountOutputCommitter:cleanupJob");
  }

  @Override
  public void commitTask(TaskAttemptContext taskContext) throws IOException {
    LOG.info("ComplexWordCountOutputCommitter:commitTask");
  }

  @Override
  public boolean needsTaskCommit(TaskAttemptContext taskContext) throws IOException {
    LOG.info("ComplexWordCountOutputCommitter:needsTaskCommit");
    return true;
  }

  @Override
  public void setupJob(JobContext jobContext) throws IOException {
    LOG.info("ComplexWordCountOutputCommitter:setupJob");
  }

  @Override
  public void setupTask(TaskAttemptContext taskContext) throws IOException {
    LOG.info("ComplexWordCountOutputCommitter:setupTask");
  }
}
