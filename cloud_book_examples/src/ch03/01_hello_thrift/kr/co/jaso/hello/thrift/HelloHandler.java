package kr.co.jaso.hello.thrift;

import org.apache.thrift.TException;
import kr.co.jaso.hello.thrift.generated.*;

public class HelloHandler implements HelloService.Iface {
  @Override
  public String greeting(String name, int age) throws TException {
      return "Hello " + name +". You are " + age + " years old";
  }
}