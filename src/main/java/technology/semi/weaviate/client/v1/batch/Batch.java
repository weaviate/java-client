package technology.semi.weaviate.client.v1.batch;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.util.BeaconPath;
import technology.semi.weaviate.client.base.util.DbVersionSupport;
import technology.semi.weaviate.client.v1.batch.api.ReferencesBatcher;
import technology.semi.weaviate.client.v1.batch.api.ObjectsBatcher;
import technology.semi.weaviate.client.v1.batch.api.ObjectsBatchDeleter;
import technology.semi.weaviate.client.v1.batch.api.ReferencePayloadBuilder;
import technology.semi.weaviate.client.v1.data.Data;

public class Batch {
  private final Config config;
  private final BeaconPath beaconPath;
  private final Data data;

  public Batch(Config config, DbVersionSupport dbVersionSupport, Data data) {
    this.config = config;
    this.beaconPath = new BeaconPath(dbVersionSupport);
    this.data = data;
  }

  public ObjectsBatcher objectsBatcher() {
    return ObjectsBatcher.create(config, data);
  }

  public ObjectsBatcher objectsBatcher(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig) {
    return ObjectsBatcher.create(config, data, batchRetriesConfig);
  }

  public ObjectsBatcher objectsAutoBatcher() {
    return ObjectsBatcher.createAuto(config, data);
  }

  public ObjectsBatcher objectsAutoBatcher(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig,
                                           ObjectsBatcher.AutoBatchConfig autoBatchConfig) {
    return ObjectsBatcher.createAuto(config, data, batchRetriesConfig, autoBatchConfig);
  }

  public ObjectsBatchDeleter objectsBatchDeleter() {
    return new ObjectsBatchDeleter(config);
  }

  public ReferencePayloadBuilder referencePayloadBuilder() {
    return new ReferencePayloadBuilder(beaconPath);
  }

  public ReferencesBatcher referencesBatcher() {
    return ReferencesBatcher.create(config);
  }
  public ReferencesBatcher referencesBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig) {
    return ReferencesBatcher.create(config, batchRetriesConfig);
  }

  public ReferencesBatcher referencesAutoBatcher() {
    return ReferencesBatcher.createAuto(config);
  }

  public ReferencesBatcher referencesAutoBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig,
                                                 ReferencesBatcher.AutoBatchConfig autoBatchConfig) {
    return ReferencesBatcher.createAuto(config, batchRetriesConfig, autoBatchConfig);
  }
}
