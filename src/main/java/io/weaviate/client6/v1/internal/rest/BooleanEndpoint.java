package io.weaviate.client6.v1.internal.rest;

import java.util.Map;
import java.util.function.Function;

public class BooleanEndpoint<RequestT> extends EndpointBase<RequestT, Boolean> {

  public static BooleanEndpoint<Void> noBody(
      Function<Void, String> method,
      Function<Void, String> requestUrl,
      Function<Void, Map<String, String>> queryParameters) {
    return new BooleanEndpoint<>(method, requestUrl, queryParameters, nullBody());
  }

  public BooleanEndpoint(
      Function<RequestT, String> method,
      Function<RequestT, String> requestUrl,
      Function<RequestT, Map<String, String>> queryParameters,
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
