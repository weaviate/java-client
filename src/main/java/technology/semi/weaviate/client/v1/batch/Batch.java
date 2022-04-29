package technology.semi.weaviate.client.v1.batch;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.batch.api.ObjectsBatchDeleter;
import technology.semi.weaviate.client.v1.batch.api.ObjectsBatcher;
import technology.semi.weaviate.client.v1.batch.api.ReferencePayloadBuilder;
import technology.semi.weaviate.client.v1.batch.api.ReferencesBatcher;

public class Batch {
  private final Config config;

  public Batch(Config config) {
    this.config = config;
  }

  public ObjectsBatcher objectsBatcher() {
    return new ObjectsBatcher(config);
  }

  public ObjectsBatchDeleter objectsBatchDeleter() {
    return new ObjectsBatchDeleter(config);
  }

  public ReferencePayloadBuilder referencePayloadBuilder() {
    return new ReferencePayloadBuilder();
  }

  public ReferencesBatcher referencesBatcher() {
    return new ReferencesBatcher(config);
  }
}
