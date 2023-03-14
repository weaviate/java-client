package io.weaviate.client.v1.data.api;

import io.weaviate.client.v1.data.util.ObjectsPath;
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

public class ObjectCreator extends BaseClient<WeaviateObject> implements ClientResult<WeaviateObject> {

  private final ObjectsPath objectsPath;
  private String id;
  private String className;
  private String consistencyLevel;
  private Map<String, Object> properties;
  private Float[] vector;

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

  public ObjectCreator withProperties(Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public ObjectCreator withVector(Float[] vector) {
    this.vector = vector;
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
            .id(getID())
            .build();
    Response<WeaviateObject> resp = sendPostRequest(path, obj, WeaviateObject.class);
    return new Result<>(resp);
  }
}
