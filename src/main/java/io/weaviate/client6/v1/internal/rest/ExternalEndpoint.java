package io.weaviate.client6.v1.internal.rest;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ExternalEndpoint<RequestT, ResponseT> extends SimpleEndpoint<RequestT, ResponseT> {

  public ExternalEndpoint(
      Function<RequestT, String> method,
      Function<RequestT, String> requestUrl,
      Function<RequestT, Map<String, String>> queryParameters,
      Function<RequestT, String> body,
      BiFunction<Integer, String, ResponseT> deserializeResponse) {
    super(method, requestUrl, queryParameters, body, deserializeResponse);
  }

  /** Returns {@link #requestUrl()} without {@code baseUrl} prefix. */
  @Override
  public String requestUrl(RestTransportOptions __, RequestT request) {
    return requestUrl(request);
  }
}
