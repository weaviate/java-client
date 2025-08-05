package io.weaviate.client6.v1.internal.rest;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.json.JSON;

public abstract class EndpointBase<RequestT, ResponseT> implements Endpoint<RequestT, ResponseT> {
  private static final Function<?, String> NULL_BODY = __ -> null;

  protected final Function<RequestT, String> method;
  protected final Function<RequestT, String> requestUrl;
  protected final Function<RequestT, String> body;
  protected final Function<RequestT, Map<String, String>> queryParameters;

  @SuppressWarnings("unchecked")
  protected static <RequestT> Function<RequestT, String> nullBody() {
    return (Function<RequestT, String>) NULL_BODY;
  }

  public EndpointBase(
      Function<RequestT, String> method,
      Function<RequestT, String> requestUrl,
      Function<RequestT, Map<String, String>> queryParameters,
      Function<RequestT, String> body) {
    this.method = method;
    this.requestUrl = requestUrl;
    this.body = body;
    this.queryParameters = queryParameters;
  }

  @Override
  public String method(RequestT request) {
    return method.apply(request);
  }

  @Override
  public String requestUrl(RequestT request) {
    return requestUrl.apply(request);
  }

  @Override
  public Map<String, String> queryParameters(RequestT request) {
    return queryParameters.apply(request);
  }

  @Override
  public String body(RequestT request) {
    return body.apply(request);
  }

  @Override
  public boolean isError(int statusCode) {
    return statusCode >= 400;
  }

  @Override
  public String deserializeError(int statusCode, String responseBody) {
    var response = JSON.deserialize(responseBody, ErrorResponse.class);
    if (response.errors.isEmpty()) {
      return "";

    }
    return response.errors.get(0).text();
  }

  static record ErrorResponse(@SerializedName("error") List<ErrorMessage> errors) {
    private static record ErrorMessage(@SerializedName("message") String text) {
    }
  }
}
