package io.weaviate.client.v1.async.batch;

import io.weaviate.client.Config;
import io.weaviate.client.base.util.BeaconPath;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.base.util.GrpcVersionSupport;
import io.weaviate.client.v1.async.batch.api.ObjectsBatchDeleter;
import io.weaviate.client.v1.async.batch.api.ObjectsBatcher;
import io.weaviate.client.v1.async.batch.api.ReferencesBatcher;
import io.weaviate.client.v1.async.data.Data;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.batch.api.ReferencePayloadBuilder;
import io.weaviate.client.v1.batch.util.ObjectsPath;
import io.weaviate.client.v1.batch.util.ReferencesPath;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

import java.util.concurrent.Executor;

public class Batch {
  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final ObjectsPath objectsPath;
  private final ReferencesPath referencesPath;
  private final BeaconPath beaconPath;
  private final Data data;
  private final GrpcVersionSupport grpcVersionSupport;
  private final AccessTokenProvider tokenProvider;

  public Batch(CloseableHttpAsyncClient client, Config config, DbVersionSupport dbVersionSupport,
               GrpcVersionSupport grpcVersionSupport, AccessTokenProvider tokenProvider, Data data) {
    this.client = client;
    this.config = config;
    this.objectsPath = new ObjectsPath();
    this.referencesPath = new ReferencesPath();
    this.beaconPath = new BeaconPath(dbVersionSupport);
    this.grpcVersionSupport = grpcVersionSupport;
    this.tokenProvider = tokenProvider;
    this.data = data;
  }

  public ObjectsBatcher objectsBatcher() {
    return objectsBatcher(ObjectsBatcher.BatchRetriesConfig.defaultConfig().build(), null);
  }

  public ObjectsBatcher objectsBatcher(Executor executor) {
    return objectsBatcher(ObjectsBatcher.BatchRetriesConfig.defaultConfig().build(), executor);
  }

  public ObjectsBatcher objectsBatcher(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig) {
    return objectsBatcher(batchRetriesConfig, null);
  }

  public ObjectsBatcher objectsBatcher(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig, Executor executor) {
    return ObjectsBatcher.create(client, config, data, objectsPath, tokenProvider, grpcVersionSupport,
      batchRetriesConfig, executor);
  }

  public ObjectsBatcher objectsAutoBatcher() {
    return objectsAutoBatcher(
      ObjectsBatcher.BatchRetriesConfig.defaultConfig().build(),
      ObjectsBatcher.AutoBatchConfig.defaultConfig().build(),
      null
    );
  }

  public ObjectsBatcher objectsAutoBatcher(Executor executor) {
    return objectsAutoBatcher(
      ObjectsBatcher.BatchRetriesConfig.defaultConfig().build(),
      ObjectsBatcher.AutoBatchConfig.defaultConfig().build(),
      executor
    );
  }

  public ObjectsBatcher objectsAutoBatcher(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig) {
    return objectsAutoBatcher(
      batchRetriesConfig,
      ObjectsBatcher.AutoBatchConfig.defaultConfig().build(),
      null
    );
  }

  public ObjectsBatcher objectsAutoBatcher(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig,
                                           Executor executor) {
    return objectsAutoBatcher(
      batchRetriesConfig,
      ObjectsBatcher.AutoBatchConfig.defaultConfig().build(),
      executor
    );
  }

  public ObjectsBatcher objectsAutoBatcher(ObjectsBatcher.AutoBatchConfig autoBatchConfig) {
    return objectsAutoBatcher(
      ObjectsBatcher.BatchRetriesConfig.defaultConfig().build(),
      autoBatchConfig,
      null
    );
  }

  public ObjectsBatcher objectsAutoBatcher(ObjectsBatcher.AutoBatchConfig autoBatchConfig,
                                           Executor executor) {
    return objectsAutoBatcher(
      ObjectsBatcher.BatchRetriesConfig.defaultConfig().build(),
      autoBatchConfig,
      executor
    );
  }

  public ObjectsBatcher objectsAutoBatcher(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig,
                                           ObjectsBatcher.AutoBatchConfig autoBatchConfig) {
    return objectsAutoBatcher(
      batchRetriesConfig,
      autoBatchConfig,
      null
    );
  }

  public ObjectsBatcher objectsAutoBatcher(ObjectsBatcher.BatchRetriesConfig batchRetriesConfig,
                                           ObjectsBatcher.AutoBatchConfig autoBatchConfig,
                                           Executor executor) {
    return ObjectsBatcher.createAuto(client, config, data, objectsPath, tokenProvider, grpcVersionSupport,
      batchRetriesConfig, autoBatchConfig, executor);
  }

  public ObjectsBatchDeleter objectsBatchDeleter() {
    return new ObjectsBatchDeleter(client, config, tokenProvider, objectsPath);
  }

  public ReferencePayloadBuilder referencePayloadBuilder() {
    return new ReferencePayloadBuilder(beaconPath);
  }

  public ReferencesBatcher referencesBatcher() {
    return referencesBatcher(ReferencesBatcher.BatchRetriesConfig.defaultConfig().build(), null);
  }

  public ReferencesBatcher referencesBatcher(Executor executor) {
    return referencesBatcher(ReferencesBatcher.BatchRetriesConfig.defaultConfig().build(), executor);
  }

  public ReferencesBatcher referencesBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig) {
    return referencesBatcher(batchRetriesConfig, null);
  }

  public ReferencesBatcher referencesBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig,
                                             Executor executor) {
    return ReferencesBatcher.create(client, config, tokenProvider, referencesPath, batchRetriesConfig, executor);
  }

  public ReferencesBatcher referencesAutoBatcher() {
    return referencesAutoBatcher(
      ReferencesBatcher.BatchRetriesConfig.defaultConfig().build(),
      ReferencesBatcher.AutoBatchConfig.defaultConfig().build(),
      null
    );
  }

  public ReferencesBatcher referencesAutoBatcher(Executor executor) {
    return referencesAutoBatcher(
      ReferencesBatcher.BatchRetriesConfig.defaultConfig().build(),
      ReferencesBatcher.AutoBatchConfig.defaultConfig().build(),
      executor
    );
  }

  public ReferencesBatcher referencesAutoBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig) {
    return referencesAutoBatcher(
      batchRetriesConfig,
      ReferencesBatcher.AutoBatchConfig.defaultConfig().build(),
      null
    );
  }

  public ReferencesBatcher referencesAutoBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig,
                                                 Executor executor) {
    return referencesAutoBatcher(
      batchRetriesConfig,
      ReferencesBatcher.AutoBatchConfig.defaultConfig().build(),
      executor
    );
  }

  public ReferencesBatcher referencesAutoBatcher(ReferencesBatcher.AutoBatchConfig autoBatchConfig) {
    return referencesAutoBatcher(
      ReferencesBatcher.BatchRetriesConfig.defaultConfig().build(),
      autoBatchConfig,
      null
    );
  }

  public ReferencesBatcher referencesAutoBatcher(ReferencesBatcher.AutoBatchConfig autoBatchConfig,
                                                 Executor executor) {
    return referencesAutoBatcher(
      ReferencesBatcher.BatchRetriesConfig.defaultConfig().build(),
      autoBatchConfig,
      executor
    );
  }

  public ReferencesBatcher referencesAutoBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig,
                                                 ReferencesBatcher.AutoBatchConfig autoBatchConfig) {
    return referencesAutoBatcher(
      batchRetriesConfig,
      autoBatchConfig,
      null
    );
  }

  public ReferencesBatcher referencesAutoBatcher(ReferencesBatcher.BatchRetriesConfig batchRetriesConfig,
                                                 ReferencesBatcher.AutoBatchConfig autoBatchConfig,
                                                 Executor executor) {
    return ReferencesBatcher.createAuto(client, config, tokenProvider, referencesPath, batchRetriesConfig, autoBatchConfig, executor);
  }
}
