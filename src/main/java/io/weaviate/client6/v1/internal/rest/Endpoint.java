package io.weaviate.client6.v1.internal.rest;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.gson.Gson;

public interface Endpoint<RequestT, ResponseT> {

  String method(RequestT request);

  String requestUrl(RequestT request);

  // Gson is leaking.
  String body(Gson gson, RequestT request);

  Map<String, String> queryParameters(RequestT request);

  /** Should this status code be considered an error? */
  boolean isError(int code);

  ResponseT deserializeResponse(Gson gson, String response);

  public static <RequestT, ResponseT> Endpoint<RequestT, ResponseT> of(
      Function<RequestT, String> method,
      Function<RequestT, String> requestUrl,
      BiFunction<Gson, RequestT, String> body,
      Function<RequestT, Map<String, String>> queryParameters,
      Function<Integer, Boolean> isError,
      BiFunction<Gson, String, ResponseT> deserialize) {
    return new Endpoint<RequestT, ResponseT>() {

      @Override
      public String method(RequestT request) {
        return method.apply(request);
      }

      @Override
      public String requestUrl(RequestT request) {
        return requestUrl.apply(request);
      }

      @Override
      public String body(Gson gson, RequestT request) {
        return body.apply(gson, request);
      }

      @Override
      public Map<String, String> queryParameters(RequestT request) {
        return queryParameters.apply(request);
      }

      @Override
      public ResponseT deserializeResponse(Gson gson, String response) {
        return deserialize.apply(gson, response);
      }

      @Override
      public boolean isError(int code) {
        return isError.apply(code);
      }
    };
  }
}
