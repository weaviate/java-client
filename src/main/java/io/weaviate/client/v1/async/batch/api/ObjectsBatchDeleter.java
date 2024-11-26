package io.weaviate.client.v1.async.batch.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.batch.model.BatchDeleteResponse;
import io.weaviate.client.v1.batch.util.ObjectsPath;
import io.weaviate.client.v1.filters.WhereFilter;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

public class ObjectsBatchDeleter extends AsyncBaseClient<BatchDeleteResponse> implements AsyncClientResult<BatchDeleteResponse> {
  private final ObjectsPath objectsPath;
  private String className;
  private String consistencyLevel;
  private String tenant;
  private WhereFilter where;
  private String output;
  private Boolean dryRun;

  public ObjectsBatchDeleter(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider, ObjectsPath objectsPath) {
    super(client, config, tokenProvider);
    this.objectsPath = objectsPath;
  }

  public ObjectsBatchDeleter withClassName(String className) {
    this.className = className;
    return this;
  }

  public ObjectsBatchDeleter withConsistencyLevel(String consistencyLevel) {
    this.consistencyLevel = consistencyLevel;
    return this;
  }

  public ObjectsBatchDeleter withTenant(String tenant) {
    this.tenant = tenant;
    return this;
  }

  public ObjectsBatchDeleter withWhere(WhereFilter where) {
    this.where = where;
    return this;
  }

  public ObjectsBatchDeleter withOutput(String output) {
    this.output = output;
    return this;
  }

  public ObjectsBatchDeleter withDryRun(Boolean dryRun) {
    this.dryRun = dryRun;
    return this;
  }

  @Override
  public Future<Result<BatchDeleteResponse>> run() {
    return run(null);
  }

  @Override
  public Future<Result<BatchDeleteResponse>> run(FutureCallback<Result<BatchDeleteResponse>> callback) {
    io.weaviate.client.v1.batch.api.ObjectsBatchDeleter.BatchDeleteMatch match = io.weaviate.client.v1.batch.api.ObjectsBatchDeleter.BatchDeleteMatch.builder()
      .className(className)
      .whereFilter(where)
      .build();
    io.weaviate.client.v1.batch.api.ObjectsBatchDeleter.BatchDelete batchDelete = io.weaviate.client.v1.batch.api.ObjectsBatchDeleter.BatchDelete.builder()
      .dryRun(dryRun)
      .output(output)
      .match(match)
      .build();
    String path = objectsPath.buildDelete(ObjectsPath.Params.builder()
      .consistencyLevel(consistencyLevel)
      .tenant(tenant)
      .build());
    return sendDeleteRequest(path, batchDelete, BatchDeleteResponse.class, callback);
  }
}
