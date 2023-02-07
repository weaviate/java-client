package technology.semi.weaviate.client.v1.classifications;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.v1.classifications.api.Getter;
import technology.semi.weaviate.client.v1.classifications.api.Scheduler;

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
