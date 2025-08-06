package io.weaviate.client6.v1.internal.rest;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.json.JSON;

public class SimpleEndpoint<RequestT, ResponseT> extends EndpointBase<RequestT, ResponseT>
    implements JsonEndpoint<RequestT, ResponseT> {
  private static final BiFunction<Integer, String, Void> NULL_RESPONSE = (__code, __body) -> null;

  private final BiFunction<Integer, String, ResponseT> deserializeResponse;

  protected static BiFunction<Integer, String, Void> nullResponse() {
    return NULL_RESPONSE;
  }

  protected static <T> BiFunction<Integer, String, T> deserializeClass(Class<T> cls) {
    return (statusCode, response) -> JSON.deserialize(response, cls);
  }

  public static <RequestT, ResponseT> SimpleEndpoint<RequestT, ResponseT> noBody(
      Function<RequestT, String> method,
      Function<RequestT, String> requestUrl,
      Function<RequestT, Map<String, Object>> queryParameters,
      BiFunction<Integer, String, ResponseT> deserializeResponse) {
    return new SimpleEndpoint<>(method, requestUrl, queryParameters, nullBody(), deserializeResponse);
  }

  public static <RequestT, ResponseT> SimpleEndpoint<RequestT, ResponseT> noBody(
      Function<RequestT, String> method,
      Function<RequestT, String> requestUrl,
      Function<RequestT, Map<String, String>> queryParameters,
      Class<ResponseT> cls) {
    return new SimpleEndpoint<>(method, requestUrl, queryParameters, nullBody(), deserializeClass(cls));
  }

  public static <RequestT> SimpleEndpoint<RequestT, Void> sideEffect(
      Function<RequestT, String> method,
      Function<RequestT, String> requestUrl,
      Function<RequestT, Map<String, Object>> queryParameters,
      Function<RequestT, String> body) {
    return new SimpleEndpoint<>(method, requestUrl, queryParameters, body, nullResponse());
  }

  public static <RequestT> SimpleEndpoint<RequestT, Void> sideEffect(
      Function<RequestT, String> method,
      Function<RequestT, String> requestUrl,
      Function<RequestT, Map<String, Object>> queryParameters) {
    return new SimpleEndpoint<>(method, requestUrl, queryParameters, nullBody(), nullResponse());
  }

  public SimpleEndpoint(
      Function<RequestT, String> method,
      Function<RequestT, String> requestUrl,
      Function<RequestT, Map<String, Object>> queryParameters,
      Function<RequestT, String> body,
      BiFunction<Integer, String, ResponseT> deserializeResponse) {
    super(method, requestUrl, queryParameters, body);
    this.deserializeResponse = deserializeResponse;
  }

  @Override
  public ResponseT deserializeResponse(int statusCode, String responseBody) {
    return deserializeResponse.apply(statusCode, responseBody);
  }
}
