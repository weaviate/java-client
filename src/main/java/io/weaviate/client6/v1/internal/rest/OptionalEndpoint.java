package io.weaviate.client6.v1.internal.rest;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class OptionalEndpoint<RequestT, ResponseT> extends SimpleEndpoint<RequestT, Optional<ResponseT>> {

  public static <RequestT, ResponseT> OptionalEndpoint<RequestT, ResponseT> noBodyOptional(
      Function<RequestT, String> method,
      Function<RequestT, String> requestUrl,
      Function<RequestT, Map<String, String>> queryParameters,
      BiFunction<Integer, String, ResponseT> deserializeResponse) {
    return new OptionalEndpoint<>(method, requestUrl, queryParameters, nullBody(), deserializeResponse);
  }

  public OptionalEndpoint(
      Function<RequestT, String> method,
      Function<RequestT, String> requestUrl,
      Function<RequestT, Map<String, String>> queryParameters,
      Function<RequestT, String> body,
      BiFunction<Integer, String, ResponseT> deserializeResponse) {
    super(method, requestUrl, queryParameters, body, optional(deserializeResponse));
  }

  private static <ResponseT> BiFunction<Integer, String, Optional<ResponseT>> optional(
      BiFunction<Integer, String, ResponseT> deserializeResponse) {
    return (statusCode, responseBody) -> statusCode == 404
        ? Optional.empty()
        : Optional.ofNullable(deserializeResponse.apply(statusCode, responseBody));
  }

  @Override
  public boolean isError(int statusCode) {
    return statusCode != 404 && super.isError(statusCode);
  }
}
