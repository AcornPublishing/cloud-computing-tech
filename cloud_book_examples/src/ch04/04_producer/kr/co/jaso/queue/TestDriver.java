package kr.co.jaso.queue;

public class TestDriver {
  public static String zkServers;
  
  static class ProducerThread extends Thread {
    Producer producer;
    public ProducerThread(String id) throws Exception {
      producer = new Producer(zkServers, id);
    }
    public void run() {
      producer.startProducer();
    }
  }

  static class ConsumerThread extends Thread {
    Consumer consumer;
    public ConsumerThread(String id) throws Exception {
      consumer = new Consumer(zkServers, id);
    }
    public void run() {
      consumer.startConsumer();
    }
  }
  
  public static void main(String[] args) throws Exception {
    if(args.length < 3) {
      System.out.println("Usage java TestDirver <zk_server> <# producer> <# consumer>");
      System.exit(0);
    }
    zkServers = args[0];
    
    for(int i = 0; i < Integer.parseInt(args[2]); i++) {
      (new ConsumerThread("" + (i + 1))).start();
    }

    for(int i = 0; i < Integer.parseInt(args[1]); i++) {
      (new ProducerThread("" + (i + 1))).start();
    }
  }
}
