package kr.co.jaso.hdfs;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFSClientExapmle {
  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    conf.set("fs.default.name", "hdfs://127.0.0.1:9000");
    FileSystem fs = FileSystem.get(conf);

    //디렉토리 생성
    String dirName = "TestDirectory";
    Path src = new Path(fs.getWorkingDirectory() + "/" + dirName);
    fs.mkdirs(src);

    //로컬 파일을 HDFS에 저장
    Path localFileSrc = new Path("/usr/local/hadoop/hadoop-0.20.2/README.txt");
    
    fs.copyFromLocalFile(localFileSrc, new Path(src, "README.txt"));

    //HDFS 파일을 로컬에 저장
    fs.copyToLocalFile(src, localFileSrc);

    //디렉토리 삭제
    fs.delete(src, true);

    // OutputStream 생성 및 데이터 저장
    fs.mkdirs(src);
    Path fileSrc = new Path(src, "/test01.dat");
    OutputStream os = fs.create(fileSrc);
    os.write("this is test".getBytes());
    os.close();

    // InputStream 생성 및 데이터 읽기
    InputStream is = fs.open(fileSrc);
    byte[] readBuf = new byte[1024];
    int readBytes;
    while ((readBytes = is.read(readBuf)) > 0) {
      System.out.println(new String(readBuf, 0, readBytes));
    }

    //기타 FileSystem 메소드
    System.out.println("Exists: " + fs.exists(src));
    FileStatus fileStatus = fs.getFileStatus(src);
    System.out.println("IsDir: " + fileStatus.isDir());
    System.out.println("List Count: " + fs.listStatus(src).length);

    System.out.println("BlockSize: " + fs.getDefaultBlockSize());
    System.out.println("Replica: " + fs.getDefaultReplication());
  }
}
