package io.weaviate.client.v1.data.api;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.util.ObjectsPath;

public class ObjectCreator extends BaseClient<WeaviateObject> implements ClientResult<WeaviateObject> {

  private final ObjectsPath objectsPath;
  private String id;
  private String className;
  private String consistencyLevel;
  private String tenant;
  private Map<String, Object> properties;
  private Float[] vector;
  private Float[][] multiVector;
  private Map<String, Float[]> vectors;
  private Map<String, Float[][]> multiVectors;

  public ObjectCreator(HttpClient httpClient, Config config, ObjectsPath objectsPath) {
    super(httpClient, config);
    this.objectsPath = Objects.requireNonNull(objectsPath);
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

  public ObjectCreator withMultiVector(Float[][] multiVector) {
    this.multiVector = multiVector;
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
  public Result<WeaviateObject> run() {
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
    Response<WeaviateObject> resp = sendPostRequest(path, obj, WeaviateObject.class);
    return new Result<>(resp);
  }
}
