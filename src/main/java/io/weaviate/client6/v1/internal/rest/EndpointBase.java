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
  protected final Function<RequestT, Map<String, Object>> queryParameters;

  @SuppressWarnings("unchecked")
  protected static <RequestT> Function<RequestT, String> nullBody() {
    return (Function<RequestT, String>) NULL_BODY;
  }

  public EndpointBase(
      Function<RequestT, String> method,
      Function<RequestT, String> requestUrl,
      Function<RequestT, Map<String, Object>> queryParameters,
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
  public Map<String, Object> queryParameters(RequestT request) {
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
    {
      var response = JSON.deserialize(responseBody, ErrorResponse1.class);
      if (response.errors != null && !response.errors.isEmpty()) {
        return response.errors.get(0).message();
      }
    }
    var response = JSON.deserialize(responseBody, ErrorResponse2.class);
    if (response.error != null && !response.error.isBlank()) {
      return response.error;
    }
    return responseBody;
  }

  private static record ErrorResponse1(@SerializedName("error") List<ErrorMessage> errors) {
    private static record ErrorMessage(@SerializedName("message") String message) {
    }
  }

  private static record ErrorResponse2(@SerializedName("message") String error) {
  }
}
