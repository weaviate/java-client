package io.weaviate.client.v1.async.classifications;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.classifications.api.Getter;
import io.weaviate.client.v1.async.classifications.api.Scheduler;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

@RequiredArgsConstructor
public class Classifications {

  private final CloseableHttpAsyncClient client;
  private final Config config;


  public Scheduler scheduler() {
    return new Scheduler(client, config, getter());
  }

  public Getter getter() {
    return new Getter(client, config);
  }
}
