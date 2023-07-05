package io.weaviate.client.v1.data.api;

import io.weaviate.client.v1.data.util.ObjectsPath;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.http.HttpClient;

public class ObjectDeleter extends BaseClient<String> implements ClientResult<Boolean> {

  private final ObjectsPath objectsPath;
  private String id;
  private String className;
  private String consistencyLevel;
  private String tenant;

  public ObjectDeleter(HttpClient httpClient, Config config, ObjectsPath objectsPath) {
    super(httpClient, config);
    this.objectsPath = Objects.requireNonNull(objectsPath);
  }

  public ObjectDeleter withID(String id) {
    this.id = id;
    return this;
  }

  public ObjectDeleter withClassName(String className) {
    this.className = className;
    return this;
  }

  public ObjectDeleter withConsistencyLevel(String consistencyLevel) {
    this.consistencyLevel = consistencyLevel;
    return this;
  }

  public ObjectDeleter withTenant(String tenant) {
    this.tenant = tenant;
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
    String path = objectsPath.buildDelete(ObjectsPath.Params.builder()
            .id(id)
            .className(className)
            .consistencyLevel(consistencyLevel)
            .tenant(tenant)
            .build());
    Response<String> resp = sendDeleteRequest(path, null, String.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 204, resp.getErrors());
  }
}
