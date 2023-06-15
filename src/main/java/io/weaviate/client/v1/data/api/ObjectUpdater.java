package io.weaviate.client.v1.data.api;

import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.util.ObjectsPath;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.http.HttpClient;

public class ObjectUpdater extends BaseClient<WeaviateObject> implements ClientResult<Boolean> {

  private final ObjectsPath objectsPath;
  private String id;
  private String className;
  private String consistencyLevel;
  private String tenantKey;
  private Map<String, Object> properties;
  private Boolean withMerge;

  public ObjectUpdater(HttpClient httpClient, Config config, ObjectsPath objectsPath) {
    super(httpClient, config);
    this.objectsPath = Objects.requireNonNull(objectsPath);
  }

  public ObjectUpdater withID(String id) {
    this.id = id;
    return this;
  }

  public ObjectUpdater withClassName(String className) {
    this.className = className;
    return this;
  }

  public ObjectUpdater withConsistencyLevel(String consistencyLevel) {
    this.consistencyLevel = consistencyLevel;
    return this;
  }

  public ObjectUpdater withTenantKey(String tenantKey) {
    this.tenantKey = tenantKey;
    return this;
  }

  public ObjectUpdater withProperties(Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public ObjectUpdater withMerge() {
    this.withMerge = true;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    if (StringUtils.isEmpty(id)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
              .message("id cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
              .error(Stream.of(errorMessage).collect(Collectors.toList())).build();
      return new Result<>(500, false, errors);
    }
    String path = objectsPath.buildUpdate(ObjectsPath.Params.builder()
            .id(id)
            .className(className)
            .consistencyLevel(consistencyLevel)
            .tenantKey(tenantKey)
            .build());
    WeaviateObject obj = WeaviateObject.builder()
            .className(className)
            .properties(properties)
            .id(id)
            .build();
    if (BooleanUtils.isTrue(withMerge)) {
      Response<WeaviateObject> resp = sendPatchRequest(path, obj, WeaviateObject.class);
      return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 204, resp.getErrors());
    }
    Response<WeaviateObject> resp = sendPutRequest(path, obj, WeaviateObject.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
