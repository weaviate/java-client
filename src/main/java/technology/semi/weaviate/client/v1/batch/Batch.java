package technology.semi.weaviate.client.v1.batch;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.util.BeaconPath;
import technology.semi.weaviate.client.base.util.DbVersionSupport;
import technology.semi.weaviate.client.v1.batch.api.ObjectsBatchDeleter;
import technology.semi.weaviate.client.v1.batch.api.ObjectsBatcher;
import technology.semi.weaviate.client.v1.batch.api.ReferencePayloadBuilder;
import technology.semi.weaviate.client.v1.batch.api.ReferencesBatcher;

public class Batch {
  private final Config config;
  private final BeaconPath beaconPath;

  public Batch(Config config, DbVersionSupport dbVersionSupport) {
    this.config = config;
    this.beaconPath = new BeaconPath(dbVersionSupport);
  }

  public ObjectsBatcher objectsBatcher() {
    return new ObjectsBatcher(config);
  }

  public ObjectsBatchDeleter objectsBatchDeleter() {
    return new ObjectsBatchDeleter(config);
  }

  public ReferencePayloadBuilder referencePayloadBuilder() {
    return new ReferencePayloadBuilder(beaconPath);
  }

  public ReferencesBatcher referencesBatcher() {
    return new ReferencesBatcher(config);
  }
}
