package kr.co.jaso.hello.zk;

import org.apache.thrift.TException;

import kr.co.jaso.hello.zk.generated.HelloService;

/**
 * IDL에서 정의한 인터페이스를 구현한 클래스
 */
public class HelloHandler implements HelloService.Iface {
  @Override
  public String greeting(String name, int age) throws TException {
    return "Hello " + name + ". You are " + age + " years old";
  }

}
