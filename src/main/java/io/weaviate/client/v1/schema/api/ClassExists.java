package io.weaviate.client.v1.schema.api;

import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.Collections;

public class ClassExists implements ClientResult<Boolean> {
  private final ClassGetter classGetter;
  private String className;

  public ClassExists(ClassGetter classGetter) {
    this.classGetter = classGetter;
  }

  public ClassExists withClassName(String className) {
    this.className = className;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    if (StringUtils.isEmpty(className)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
        .message("classname cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
        .error(Collections.singletonList(errorMessage)).build();
      return new Result<>(HttpStatus.SC_UNPROCESSABLE_ENTITY, null, errors);
    }

    Result<WeaviateClass> getterClass = classGetter.withClassName(className).run();
    if (getterClass.hasErrors()) {
      WeaviateError error = getterClass.getError();
      return new Result<>(error.getStatusCode(), null, WeaviateErrorResponse.builder().error(error.getMessages()).build());
    }
    return new Result<>(HttpStatus.SC_OK, getterClass.getResult() != null, null);
  }
}
