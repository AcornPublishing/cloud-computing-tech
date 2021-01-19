package kr.co.jaso.hadoop.wordcount;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class GenTestData {
  public static void main(String[] args) throws Exception {
    args = new String[]{"/Users/babokim/workspace/cassandra/CHANGES.txt", "/Users/babokim/temp/maptest.dat"};
    FileInputStream in = new FileInputStream(new File(args[0]));
    FileOutputStream out = new FileOutputStream(new File(args[1]));
    
    byte[] buf = new byte[4096];
    int readBytes = 0;
    
    while( (readBytes = in.read(buf)) > 0) {
      for(int i = 0 ;i < 10000; i++) {
        out.write(buf, 0, readBytes);
      }
    }
    
    in.close();
    out.close();
  }
}
