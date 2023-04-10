package io.weaviate.client.v1.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassExists implements ClientResult<Boolean> {
  private final ClassGetter classGetter;
  private String className;

  public ClassExists(HttpClient httpClient, Config config) {
    this.classGetter = new ClassGetter(httpClient, config);
  }

  public ClassExists withClassName(String className) {
    this.className = className;
    return this;
  }

  private Result<Boolean> toResult(WeaviateError error) {
    return new Result<>(error.getStatusCode(), null, WeaviateErrorResponse.builder().error(error.getMessages()).build());
  }

  @Override
  public Result<Boolean> run() {
    if (StringUtils.isEmpty(this.className)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
        .message("classname cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
        .error(Stream.of(errorMessage).collect(Collectors.toList())).build();
      return new Result<>(500, null, errors);
    }

    Result<WeaviateClass> getterClass = this.classGetter.withClassName(this.className).run();
    if (getterClass.hasErrors()) {
      return toResult(getterClass.getError());
    }
    return new Result<>(HttpStatus.SC_OK, getterClass.getResult() != null, null);
  }
}
