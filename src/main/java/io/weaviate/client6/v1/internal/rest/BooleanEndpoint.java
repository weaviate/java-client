package io.weaviate.client6.v1.internal.rest;

import java.util.Map;
import java.util.function.Function;

public class BooleanEndpoint<RequestT> extends EndpointBase<RequestT, Boolean> {

  public BooleanEndpoint(
      Function<RequestT, String> method,
      Function<RequestT, String> requestUrl,
      Function<RequestT, Map<String, Object>> queryParameters,
      Function<RequestT, String> body) {
    super(method, requestUrl, queryParameters, body);
  }

  @Override
  public boolean isError(int statusCode) {
    return statusCode != 404 && super.isError(statusCode);
  }

  public boolean getResult(int statusCode) {
    return statusCode < 400;
  }
}
