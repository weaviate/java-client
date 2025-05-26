package io.weaviate.client6.v1.internal.rest;

import java.util.Map;

import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;

import com.google.gson.Gson;

public interface Endpoint<RequestT, ResponseT> {

  String method(RequestT request);

  String requestUrl(RequestT request);

  // Gson is leaking.
  String body(Gson gson, RequestT request);

  Map<String, String> queryParameters(RequestT request);

  /** Should this status code be considered an error? */
  boolean isError();

  ResponseT deserializeResponse(Gson gson, ClassicHttpResponse response);

  ResponseT deserializeResponse(Gson gson, SimpleHttpResponse response);
}
