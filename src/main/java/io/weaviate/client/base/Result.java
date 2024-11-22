package io.weaviate.client.base;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Result<T> {
  T result;
  WeaviateError error;

  public Result(Response<T> response) {
    this(response.getStatusCode(), response.getBody(), response.getErrors());
  }

  public Result(int statusCode, T body, WeaviateErrorResponse errors) {
    if (errors != null && errors.getError() != null) {
      List<WeaviateErrorMessage> items = errors.getError().stream().filter(Objects::nonNull).collect(Collectors.toList());
      this.error = new WeaviateError(statusCode, items);
      this.result = body;
    } else if (errors != null && errors.getMessage() != null) {
      this.error = new WeaviateError(statusCode, Collections.singletonList(WeaviateErrorMessage.builder().message(errors.getMessage()).build()));
      this.result = body;
    } else {
      this.result = body;
      this.error = null;
    }
  }

  /**
   * Copy the Result object with a null body, preserving only the status code and the error message.
   * 
   * @param <NULL> Would-be response type. It's required for type safety, but can be anything since the body is always set to null.
   * @return Result
   */
  public <NULL> Result<NULL> toErrorResult() {
    return new Result<>(this.error.getStatusCode(), null, WeaviateErrorResponse.builder().error(this.error.getMessages()).build());
  }

  public boolean hasErrors() {
    return this.error != null;
  }
}
