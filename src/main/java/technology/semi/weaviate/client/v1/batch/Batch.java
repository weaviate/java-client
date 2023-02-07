package technology.semi.weaviate.client.v1.batch;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.base.util.BeaconPath;
import technology.semi.weaviate.client.base.util.DbVersionSupport;
import technology.semi.weaviate.client.v1.batch.api.ReferencesBatcher;
import technology.semi.weaviate.client.v1.batch.api.ObjectsBatcher;
import technology.semi.weaviate.client.v1.batch.api.ObjectsBatchDeleter;
import technology.semi.weaviate.client.v1.batch.api.ReferencePayloadBuilder;
import technology.semi.weaviate.client.v1.data.Data;

public class Batch {
  private final Config config;
  private final HttpClient httpClient;
  private final BeaconPath beaconPath;
  private final Data data;

  public Batch(HttpClient httpClient, Config config, DbVersionSupport dbVersionSupport, Data data) {
    this.config = config;
    this.httpClient = httpClient;
    this.beaconPath = new BeaconPath(dbVersionSupport);
    this.data = data;
  }

  public ObjectsBatcher objectsBatcher() {
    return objectsBatcher(ObjectsBatcher.BatchRetriesConfig.defaultConfig().build());
  }

  public ObjectsBatcher objectsBatcher(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig) {
    return ObjectsBatcher.create(httpClient, config, data, batchRetriesConfig);
  }

  public ObjectsBatcher objectsAutoBatcher() {
    return objectsAutoBatcher(
      ObjectsBatcher.BatchRetriesConfig.defaultConfig().build(),
      ObjectsBatcher.AutoBatchConfig.defaultConfig().build()
    );
  }

  public ObjectsBatcher objectsAutoBatcher(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig) {
    return objectsAutoBatcher(
      batchRetriesConfig,
      ObjectsBatcher.AutoBatchConfig.defaultConfig().build()
    );
  }

  public ObjectsBatcher objectsAutoBatcher(ObjectsBatcher.AutoBatchConfig autoBatchConfig) {
    return objectsAutoBatcher(
      ObjectsBatcher.BatchRetriesConfig.defaultConfig().build(),
      autoBatchConfig
    );
  }

  public ObjectsBatcher objectsAutoBatcher(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig,
                                           ObjectsBatcher.AutoBatchConfig autoBatchConfig) {
    return ObjectsBatcher.createAuto(httpClient, config, data, batchRetriesConfig, autoBatchConfig);
  }

  public ObjectsBatchDeleter objectsBatchDeleter() {
    return new ObjectsBatchDeleter(httpClient, config);
  }

  public ReferencePayloadBuilder referencePayloadBuilder() {
    return new ReferencePayloadBuilder(beaconPath);
  }

  public ReferencesBatcher referencesBatcher() {
    return referencesBatcher(ReferencesBatcher.BatchRetriesConfig.defaultConfig().build());
  }
  public ReferencesBatcher referencesBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig) {
    return ReferencesBatcher.create(httpClient, config, batchRetriesConfig);
  }

  public ReferencesBatcher referencesAutoBatcher() {
    return referencesAutoBatcher(
      ReferencesBatcher.BatchRetriesConfig.defaultConfig().build(),
      ReferencesBatcher.AutoBatchConfig.defaultConfig().build()
    );
  }

  public ReferencesBatcher referencesAutoBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig) {
    return referencesAutoBatcher(
      batchRetriesConfig,
      ReferencesBatcher.AutoBatchConfig.defaultConfig().build()
    );
  }

  public ReferencesBatcher referencesAutoBatcher(ReferencesBatcher.AutoBatchConfig autoBatchConfig) {
    return referencesAutoBatcher(
      ReferencesBatcher.BatchRetriesConfig.defaultConfig().build(),
      autoBatchConfig
    );
  }

  public ReferencesBatcher referencesAutoBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig,
                                                 ReferencesBatcher.AutoBatchConfig autoBatchConfig) {
    return ReferencesBatcher.createAuto(httpClient, config, batchRetriesConfig, autoBatchConfig);
  }
}
