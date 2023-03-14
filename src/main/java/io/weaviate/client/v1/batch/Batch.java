package io.weaviate.client.v1.batch;

import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.BeaconPath;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.v1.batch.api.ObjectsBatchDeleter;
import io.weaviate.client.v1.batch.api.ObjectsBatcher;
import io.weaviate.client.v1.batch.api.ReferencePayloadBuilder;
import io.weaviate.client.v1.batch.api.ReferencesBatcher;
import io.weaviate.client.v1.batch.util.ObjectsPath;
import io.weaviate.client.v1.batch.util.ReferencesPath;
import io.weaviate.client.Config;
import io.weaviate.client.v1.data.Data;

public class Batch {
  private final Config config;
  private final HttpClient httpClient;
  private final BeaconPath beaconPath;
  private final ObjectsPath objectsPath;
  private final ReferencesPath referencesPath;
  private final Data data;

  public Batch(HttpClient httpClient, Config config, DbVersionSupport dbVersionSupport, Data data) {
    this.config = config;
    this.httpClient = httpClient;
    this.beaconPath = new BeaconPath(dbVersionSupport);
    this.objectsPath = new ObjectsPath();
    this.referencesPath = new ReferencesPath();
    this.data = data;
  }

  public ObjectsBatcher objectsBatcher() {
    return objectsBatcher(ObjectsBatcher.BatchRetriesConfig.defaultConfig().build());
  }

  public ObjectsBatcher objectsBatcher(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig) {
    return ObjectsBatcher.create(httpClient, config, data, objectsPath, batchRetriesConfig);
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
    return ObjectsBatcher.createAuto(httpClient, config, data, objectsPath, batchRetriesConfig, autoBatchConfig);
  }

  public ObjectsBatchDeleter objectsBatchDeleter() {
    return new ObjectsBatchDeleter(httpClient, config, objectsPath);
  }

  public ReferencePayloadBuilder referencePayloadBuilder() {
    return new ReferencePayloadBuilder(beaconPath);
  }

  public ReferencesBatcher referencesBatcher() {
    return referencesBatcher(ReferencesBatcher.BatchRetriesConfig.defaultConfig().build());
  }
  public ReferencesBatcher referencesBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig) {
    return ReferencesBatcher.create(httpClient, config, referencesPath, batchRetriesConfig);
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
    return ReferencesBatcher.createAuto(httpClient, config, referencesPath, batchRetriesConfig, autoBatchConfig);
  }
}
