package io.weaviate.client.base;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client.base.http.async.ResponseParser;
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
      List<WeaviateErrorMessage> items = errors.getError().stream().filter(Objects::nonNull)
          .collect(Collectors.toList());
      this.error = new WeaviateError(statusCode, items);
      this.result = body;
    } else if (errors != null && errors.getMessage() != null) {
      this.error = new WeaviateError(statusCode,
          Collections.singletonList(WeaviateErrorMessage.builder().message(errors.getMessage()).build()));
      this.result = body;
    } else {
      this.result = body;
      this.error = null;
    }
  }

  public boolean hasErrors() {
    return this.error != null;
  }

  /**
   * Copy the Result object with a null body, preserving only the status code and
   * the error message.
   *
   * @param <C> Would-be response type. It's required for type safety, but can be
   *            anything since the body is always set to null.
   * @return A copy of this Result.
   */
  public <C> Result<C> toErrorResult() {
    return new Result<>(this.error.getStatusCode(), null,
        WeaviateErrorResponse.builder().error(this.error.getMessages()).build());
  }

  /**
   * Convert {@code Result<Void>} response to a {@code Result<Boolean>}.
   * Returns true if response status code is 200.
   *
   * @param response Response from a call that does not return a value, like
   *                 {@link BaseClient#sendDeleteRequest}.
   * @return {@code Result<Boolean>}
   */
  public static Result<Boolean> voidToBoolean(Response<Void> response) {
    int status = response.getStatusCode();
    return new Result<>(status, status == 200, response.getErrors());
  }

  /**
   * Get a custom parser to convert {@code Result<Void>} response as to a
   * {@code Result<Void>}. Result is true if response status code is 200.
   *
   * @param response Response from an async call that does not return a value,
   *                 like {@link AsyncBaseClient#sendDeleteRequest}.
   * @return {@code Result<Boolean>}
   */
  public static ResponseParser<Boolean> voidToBooleanParser() {
    return new ResponseParser<Boolean>() {
      @Override
      public Result<Boolean> parse(HttpResponse response, String body, ContentType contentType) {
        Response<Object> resp = this.serializer.toResponse(response.getCode(), body, Object.class);
        return new Result<>(resp.getStatusCode(), resp.getStatusCode() == HttpStatus.SC_OK, resp.getErrors());
      }
    };
  }

}
