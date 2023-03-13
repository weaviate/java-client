package io.weaviate.client.v1.schema.api;

import io.weaviate.client.v1.schema.model.WeaviateClass;
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

public class ClassGetter extends BaseClient<WeaviateClass> implements ClientResult<WeaviateClass> {

  private String className;

  public ClassGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public ClassGetter withClassName(String className) {
    this.className = className;
    return this;
  }

  @Override
  public Result<WeaviateClass> run() {
    if (StringUtils.isEmpty(this.className)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
              .message("classname cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
              .error(Stream.of(errorMessage).collect(Collectors.toList())).build();
      return new Result<>(500, null, errors);
    }
    String path = String.format("/schema/%s", this.className);
    Response<WeaviateClass> resp = sendGetRequest(path, WeaviateClass.class);
    return new Result<>(resp);
  }
}
