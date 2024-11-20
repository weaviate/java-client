package io.weaviate.client.v1.async.batch;

import io.weaviate.client.Config;
import io.weaviate.client.base.util.BeaconPath;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.base.util.GrpcVersionSupport;
import io.weaviate.client.v1.async.batch.api.ObjectsBatchDeleter;
import io.weaviate.client.v1.async.batch.api.ObjectsBatcher;
import io.weaviate.client.v1.async.data.Data;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.batch.api.ReferencePayloadBuilder;
import io.weaviate.client.v1.batch.util.ObjectsPath;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

public class Batch {
  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final ObjectsPath objectsPath;
  private final BeaconPath beaconPath;
  private final Data data;
  private final GrpcVersionSupport grpcVersionSupport;
  private final AccessTokenProvider tokenProvider;

  public Batch(CloseableHttpAsyncClient client, Config config, DbVersionSupport dbVersionSupport,
    GrpcVersionSupport grpcVersionSupport, AccessTokenProvider tokenProvider, Data data) {
    this.client = client;
    this.config = config;
    this.objectsPath = new ObjectsPath();
    this.beaconPath = new BeaconPath(dbVersionSupport);
    this.grpcVersionSupport = grpcVersionSupport;
    this.tokenProvider = tokenProvider;
    this.data = data;
  }

  public ObjectsBatcher objectsBatcher() {
    return objectsBatcher(ObjectsBatcher.BatchRetriesConfig.defaultConfig().build());
  }

  public ObjectsBatcher objectsBatcher(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig) {
    return ObjectsBatcher.create(client, config, data, objectsPath, tokenProvider, grpcVersionSupport, batchRetriesConfig);
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
    return ObjectsBatcher.createAuto(client, config, data, objectsPath, tokenProvider, grpcVersionSupport, batchRetriesConfig, autoBatchConfig);
  }

  public ObjectsBatchDeleter objectsBatchDeleter() {
    return new ObjectsBatchDeleter(client, config, objectsPath);
  }

  public ReferencePayloadBuilder referencePayloadBuilder() {
    return new ReferencePayloadBuilder(beaconPath);
  }

  // TODO: implement async ReferencesBatcher
//  public ReferencesBatcher referencesBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig) {
//    return ReferencesBatcher.create(httpClient, config, referencesPath, batchRetriesConfig);
//  }
//
//  public ReferencesBatcher referencesAutoBatcher() {
//    return referencesAutoBatcher(
//      ReferencesBatcher.BatchRetriesConfig.defaultConfig().build(),
//      ReferencesBatcher.AutoBatchConfig.defaultConfig().build()
//    );
//  }
//
//  public ReferencesBatcher referencesAutoBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig) {
//    return referencesAutoBatcher(
//      batchRetriesConfig,
//      ReferencesBatcher.AutoBatchConfig.defaultConfig().build()
//    );
//  }
//
//  public ReferencesBatcher referencesAutoBatcher(ReferencesBatcher.AutoBatchConfig autoBatchConfig) {
//    return referencesAutoBatcher(
//      ReferencesBatcher.BatchRetriesConfig.defaultConfig().build(),
//      autoBatchConfig
//    );
//  }
//
//  public ReferencesBatcher referencesAutoBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig,
//    ReferencesBatcher.AutoBatchConfig autoBatchConfig) {
//    return ReferencesBatcher.createAuto(httpClient, config, referencesPath, batchRetriesConfig, autoBatchConfig);
//  }
}
