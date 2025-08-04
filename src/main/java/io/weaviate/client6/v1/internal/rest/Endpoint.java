package io.weaviate.client6.v1.internal.rest;

import java.util.Map;

public interface Endpoint<RequestT, ResponseT> {

  String method(RequestT request);

  String requestUrl(RequestT request);

  String body(RequestT request);

  Map<String, String> queryParameters(RequestT request);

  /** Should this status code be considered an error? */
  boolean isError(int statusCode);

  String deserializeError(int statusCode, String responseBody);
}
