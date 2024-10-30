package io.weaviate.client.base;

import java.util.Collections;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.http.HttpResponse;

public abstract class BaseClient<T> {
  private final HttpClient client;
  private final Config config;
  private final Serializer serializer;

  public BaseClient(HttpClient client, Config config) {
    this.config = config;
    this.client = client;
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
        WeaviateErrorResponse errors = null;

        if (SneakyError.class.isAssignableFrom(classOfT)) {
          errors = fromSneakyError((SneakyError) body, statusCode);
        }
        return new Response<>(statusCode, body, errors);
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
    WeaviateErrorMessage error = WeaviateErrorMessage.builder().message(e.getMessage()).throwable(e).build();
    return WeaviateErrorResponse.builder().error(Collections.singletonList(error)).build();
  }

  /**
   * Build {@link WeaviateErrorResponse} from a response body with a "sneaky error".
   * 
   * @param sneaky Response body containing error messages.
   * @param code   HTTP status code to pass in the {@link WeaviateErrorResponse}.
   * @return Error response to be returned to the caller.
   */
  private WeaviateErrorResponse fromSneakyError(SneakyError sneaky, int code) {
    return WeaviateErrorResponse.builder().code(code).error(sneaky.errorMessages()).build();
  }
}
