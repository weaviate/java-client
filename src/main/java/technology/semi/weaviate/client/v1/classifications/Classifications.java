package technology.semi.weaviate.client.v1.classifications;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.classifications.api.Getter;
import technology.semi.weaviate.client.v1.classifications.api.Scheduler;

public class Classifications {
  private final Config config;

  public Classifications(Config config) {
    this.config = config;
  }

  public Scheduler scheduler() {
    return new Scheduler(config);
  }

  public Getter getter() {
    return new Getter(config);
  }
}
