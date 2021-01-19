package kr.co.jaso.hello.avro;

import java.net.URL;

import kr.co.jaso.hello.avro.generated.Greeting;
import kr.co.jaso.hello.avro.generated.HelloService;

import org.apache.avro.ipc.HttpTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.specific.SpecificRequestor;
import org.apache.avro.util.Utf8;

public class HelloClient {
  public static void main(String[] args) throws Exception {
    Transceiver client = new HttpTransceiver(new URL("http://127.0.0.1:9090"));
    HelloService proxy = (HelloService) SpecificRequestor.getClient(HelloService.class, client);
    Greeting result = proxy.hello(new Utf8("kim"));
    System.out.println(result.greetingMessage.toString());
    client.close();
  }
}
