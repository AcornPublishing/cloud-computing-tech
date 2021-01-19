package kr.co.jaso.hello.avro.generated;

@SuppressWarnings("all")
public interface HelloService {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"HelloService\",\"namespace\":\"kr.co.jaso.hello.avro.generated\",\"types\":[{\"type\":\"record\",\"name\":\"Greeting\",\"fields\":[{\"name\":\"greetingMessage\",\"type\":\"string\"}]},{\"type\":\"error\",\"name\":\"GreetingException\",\"fields\":[{\"name\":\"message\",\"type\":\"string\"}]}],\"messages\":{\"hello\":{\"request\":[{\"name\":\"greeting\",\"type\":\"string\"}],\"response\":\"Greeting\",\"errors\":[\"GreetingException\"]}}}");
  kr.co.jaso.hello.avro.generated.Greeting hello(org.apache.avro.util.Utf8 greeting)
    throws org.apache.avro.ipc.AvroRemoteException, kr.co.jaso.hello.avro.generated.GreetingException;
}
