package io.weaviate.client.v1.data.api;

import io.weaviate.client.v1.data.model.WeaviateObject;
import java.util.Map;
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

public class ObjectValidator extends BaseClient<WeaviateObject> implements ClientResult<Boolean> {

  private String id;
  private String className;
  private Map<String, Object> properties;

  public ObjectValidator(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public ObjectValidator withID(String id) {
    this.id = id;
    return this;
  }

  public ObjectValidator withClassName(String className) {
    this.className = className;
    return this;
  }

  public ObjectValidator withProperties(Map<String, Object> properties) {
    this.properties = properties;
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
    WeaviateObject obj = WeaviateObject.builder()
            .className(className)
            .properties(properties)
            .id(id)
            .build();
    Response<WeaviateObject> resp = sendPostRequest("/objects/validate", obj, WeaviateObject.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == 200, resp.getErrors());
  }
}
