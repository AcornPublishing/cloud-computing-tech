package kr.co.jaso.hello.avro;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import org.apache.avro.tool.RpcSendTool;

public class HelloClient2 {
  public static void main(String[] args) throws Exception {
    if(args.length < 1) {
      System.out.println("Usage java HelloClient2 <avpr path>");
      System.exit(0);
    }
    String protocolFile = args[0]; //"/Users/babokim/workspace/cloud_book_examples/src/ch03/03_hello_avro/kr/co/jaso/hello/avro/idl/hello.avpr";
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    RpcSendTool tool = new RpcSendTool();
    tool.run(null, new PrintStream(bout), System.err, Arrays.asList(
        "http://127.0.0.1:9090/", protocolFile, "hello", "-data",
        "{ \"greeting\": \"kim\" }"));

    System.out.println(new String(bout.toByteArray()));
  }
}
