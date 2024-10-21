package io.weaviate.client.v1.data.api;

import io.weaviate.client.v1.data.util.ObjectsPath;

import java.util.Collections;
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
import org.apache.hc.core5.http.HttpStatus;

public class ObjectsChecker extends BaseClient<String> implements ClientResult<Boolean> {

  private final ObjectsPath objectsPath;
  private String id;
  private String className;
  private String tenant;

  public ObjectsChecker(HttpClient httpClient, Config config, ObjectsPath objectsPath) {
    super(httpClient, config);
    this.objectsPath = Objects.requireNonNull(objectsPath);
  }

  public ObjectsChecker withID(String id) {
    this.id = id;
    return this;
  }

  public ObjectsChecker withClassName(String className) {
    this.className = className;
    return this;
  }

  public ObjectsChecker withTenant(String tenant) {
    this.tenant = tenant;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    if (StringUtils.isEmpty(this.id)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
              .message("id cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
              .error(Stream.of(errorMessage).collect(Collectors.toList())).build();
      return new Result<>(500, false, errors);
    }
    String path = objectsPath.buildCheck(ObjectsPath.Params.builder()
            .id(id)
            .className(className)
            .tenant(tenant)
            .build());
    Response<String> resp = sendHeadRequest(path, String.class);

    switch (resp.getStatusCode()) {
      case HttpStatus.SC_NO_CONTENT:
      case HttpStatus.SC_NOT_FOUND:
        return new Result<>(resp.getStatusCode(), resp.getStatusCode() == HttpStatus.SC_NO_CONTENT, resp.getErrors());
      default:
        WeaviateErrorResponse dummyError = WeaviateErrorResponse.builder().error(Collections.emptyList()).build();
        return new Result<>(resp.getStatusCode(), resp.getStatusCode() == HttpStatus.SC_NO_CONTENT, dummyError);
    }
  }
}
