package io.dma.client;

/**
 * This interface has to be on java side, otherwise it will provoke a cycle dependencies between
 * java<->scala code<br/>
 * 
 * And we need compile order by java first to use lombok... sad
 * 
 * @param <T>
 */
public interface StreamingClient<T> {
  void start();

  void stop();

  void publishToQueue(T payload);

}
