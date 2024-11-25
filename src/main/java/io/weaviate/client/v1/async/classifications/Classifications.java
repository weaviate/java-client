package io.weaviate.client.v1.async.classifications;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.classifications.api.Getter;
import io.weaviate.client.v1.async.classifications.api.Scheduler;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class Classifications {

  private final CloseableHttpAsyncClient client;
  private final Config config;


  public Scheduler scheduler() {
    return scheduler(null);
  }

  public Scheduler scheduler(Executor executor) {
    return new Scheduler(client, config, getter(), executor);
  }

  public Getter getter() {
    return new Getter(client, config);
  }
}
