package technology.semi.weaviate.client.base;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.base.http.HttpResponse;
import technology.semi.weaviate.client.base.http.impl.CommonsHttpClientImpl;

public abstract class BaseClient<T> {
  private final HttpClient client;
  private final Config config;
  private final Serializer serializer;

  public BaseClient(Config config) {
    HttpClientBuilder builder = HttpClientBuilder.create();
    builder.setDefaultRequestConfig(buildTimeoutConfig(config));
    this.config = config;
    this.client = new CommonsHttpClientImpl(config.getHeaders(), builder::build);
    this.serializer = new Serializer();
  }

  protected Response<T> sendGetRequest(String endpoint, Class<T> classOfT) {
    return sendRequest(endpoint, null, "GET", classOfT);
  }

  protected Response<T> sendPostRequest(String endpoint, Object payload, Class<T> classOfT) {
    return sendRequest(endpoint, payload, "POST", classOfT);
  }

  protected Response<T> sendPutRequest(String endpoint, Object payload, Class<T> classOfT) {
    return sendRequest(endpoint, payload, "PUT", classOfT);
  }

  protected Response<T> sendPatchRequest(String endpoint, Object payload, Class<T> classOfT) {
    return sendRequest(endpoint, payload, "PATCH", classOfT);
  }

  protected Response<T> sendDeleteRequest(String endpoint, Object payload, Class<T> classOfT) {
    return sendRequest(endpoint, payload, "DELETE", classOfT);
  }

  protected Response<T> sendHeadRequest(String endpoint, Class<T> classOfT) {
    return sendRequest(endpoint, null, "HEAD", classOfT);
  }

  private Response<T> sendRequest(String endpoint, Object payload, String method, Class<T> classOfT) {
    try {
      String url = config.getBaseURL() + endpoint;
      String json = toJsonString(payload);
      HttpResponse response = this.sendHttpRequest(url, json, method);
      int statusCode = response.getStatusCode();
      String responseBody = response.getBody();

      if (statusCode < 399) {
        T body = toResponse(responseBody, classOfT);
        return new Response<>(statusCode, body, null);
      }

      WeaviateErrorResponse error = toResponse(responseBody, WeaviateErrorResponse.class);
      return new Response<>(statusCode, null, error);
    } catch (Exception e) {
      WeaviateErrorResponse errors = getWeaviateErrorResponse(e);
      return new Response<>(0, null, errors);
    }
  }

  private HttpResponse sendHttpRequest(String address, String json, String method) throws Exception {
    if (method.equals("POST")) {
      return client.sendPostRequest(address, json);
    }
    if (method.equals("PUT")) {
      return client.sendPutRequest(address, json);
    }
    if (method.equals("PATCH")) {
      return client.sendPatchRequest(address, json);
    }
    if (method.equals("DELETE")) {
      return client.sendDeleteRequest(address, json);
    }
    if (method.equals("HEAD")) {
      return client.sendHeadRequest(address);
    }
    return client.sendGetRequest(address);
  }

  private <C> C toResponse(String response, Class<C> classOfT) {
    return serializer.toResponse(response, classOfT);
  }

  private String toJsonString(Object object) {
    return serializer.toJsonString(object);
  }

  private WeaviateErrorResponse getWeaviateErrorResponse(Exception e) {
    WeaviateErrorMessage error = WeaviateErrorMessage.builder().message(e.getMessage()).build();
    return WeaviateErrorResponse.builder().error(Stream.of(error).collect(Collectors.toList())).build();
  }

  private RequestConfig buildTimeoutConfig(Config config) {
    return RequestConfig.custom()
      .setConnectTimeout(config.getConnectionTimeout() * 1000)
      .setConnectionRequestTimeout(config.getConnectionRequestTimeout() * 1000)
      .setSocketTimeout(config.getSocketTimeout() * 1000)
      .build();
  }

}
