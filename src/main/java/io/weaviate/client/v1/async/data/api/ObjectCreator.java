package io.weaviate.client.v1.async.data.api;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.util.ObjectsPath;

public class ObjectCreator extends AsyncBaseClient<WeaviateObject> implements AsyncClientResult<WeaviateObject> {
  private final ObjectsPath objectsPath;
  private String id;
  private String className;
  private String consistencyLevel;
  private String tenant;
  private Map<String, Object> properties;
  private Float[] vector;
  private Map<String, Float[]> vectors;
  private Map<String, Float[][]> multiVectors;

  public ObjectCreator(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider,
      ObjectsPath objectsPath) {
    super(client, config, tokenProvider);
    this.objectsPath = objectsPath;
  }

  public ObjectCreator withID(String id) {
    this.id = id;
    return this;
  }

  public ObjectCreator withClassName(String className) {
    this.className = className;
    return this;
  }

  public ObjectCreator withConsistencyLevel(String consistencyLevel) {
    this.consistencyLevel = consistencyLevel;
    return this;
  }

  public ObjectCreator withTenant(String tenant) {
    this.tenant = tenant;
    return this;
  }

  public ObjectCreator withProperties(Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public ObjectCreator withVector(Float[] vector) {
    this.vector = vector;
    return this;
  }

  public ObjectCreator withVectors(Map<String, Float[]> vectors) {
    this.vectors = vectors;
    return this;
  }

  public ObjectCreator withMultiVectors(Map<String, Float[][]> multiVectors) {
    this.multiVectors = multiVectors;
    return this;
  }

  private String getID() {
    if (StringUtils.isEmpty(id)) {
      return UUID.randomUUID().toString();
    }
    return id;
  }

  @Override
  public Future<Result<WeaviateObject>> run(FutureCallback<Result<WeaviateObject>> callback) {
    String path = objectsPath.buildCreate(ObjectsPath.Params.builder()
        .consistencyLevel(consistencyLevel)
        .build());
    WeaviateObject obj = WeaviateObject.builder()
        .className(className)
        .properties(properties)
        .vector(vector)
        .vectors(vectors)
        .multiVectors(multiVectors)
        .id(getID())
        .tenant(tenant)
        .build();
    return sendPostRequest(path, obj, WeaviateObject.class, callback);
  }
}
