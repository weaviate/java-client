package io.weaviate.client.v1.classifications;

import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.classifications.api.Getter;
import io.weaviate.client.v1.classifications.api.Scheduler;
import io.weaviate.client.Config;

public class Classifications {
  private final Config config;
  private final HttpClient httpClient;

  public Classifications(HttpClient httpClient, Config config) {
    this.config = config;
    this.httpClient = httpClient;
  }

  public Scheduler scheduler() {
    return new Scheduler(httpClient, config);
  }

  public Getter getter() {
    return new Getter(httpClient, config);
  }
}
