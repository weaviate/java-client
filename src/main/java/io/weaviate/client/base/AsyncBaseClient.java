package io.weaviate.client.base;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.async.ResponseParser;
import io.weaviate.client.base.http.async.WeaviateResponseConsumer;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;

public abstract class AsyncBaseClient<T> {
  protected final CloseableHttpAsyncClient client;
  private final Config config;
  private final Serializer serializer;

  public AsyncBaseClient(CloseableHttpAsyncClient client, Config config) {
    this.client = client;
    this.config = config;
    this.serializer = new Serializer();
  }

  protected Future<Result<T>> sendGetRequest(String endpoint, Class<T> classOfT, FutureCallback<Result<T>> callback) {
    return sendRequest(endpoint, null, "GET", classOfT, callback, null);
  }

  protected Future<Result<T>> sendGetRequest(String endpoint, FutureCallback<Result<T>> callback, ResponseParser<T> parser) {
    return sendRequest(endpoint, null, "GET", null, callback, parser);
  }

  protected Future<Result<T>> sendPostRequest(String endpoint, Object payload, Class<T> classOfT, FutureCallback<Result<T>> callback) {
    return sendRequest(endpoint, payload, "POST", classOfT, callback, null);
  }

  protected Future<Result<T>> sendPostRequest(String endpoint, Object payload, FutureCallback<Result<T>> callback, ResponseParser<T> parser) {
    return sendRequest(endpoint, payload, "POST", null, callback, parser);
  }

  protected Future<Result<T>> sendPutRequest(String endpoint, Object payload, Class<T> classOfT, FutureCallback<Result<T>> callback) {
    return sendRequest(endpoint, payload, "PUT", classOfT, callback, null);
  }

  protected Future<Result<T>> sendPutRequest(String endpoint, Object payload, FutureCallback<Result<T>> callback, ResponseParser<T> parser) {
    return sendRequest(endpoint, payload, "PUT", null, callback, parser);
  }

  protected Future<Result<T>> sendPatchRequest(String endpoint, Object payload, Class<T> classOfT, FutureCallback<Result<T>> callback) {
    return sendRequest(endpoint, payload, "PATCH", classOfT, callback, null);
  }

  protected Future<Result<T>> sendPatchRequest(String endpoint, Object payload, FutureCallback<Result<T>> callback, ResponseParser<T> parser) {
    return sendRequest(endpoint, payload, "PATCH", null, callback, parser);
  }

  protected Future<Result<T>> sendDeleteRequest(String endpoint, Object payload, Class<T> classOfT, FutureCallback<Result<T>> callback) {
    return sendRequest(endpoint, payload, "DELETE", classOfT, callback, null);
  }

  protected Future<Result<T>> sendDeleteRequest(String endpoint, Object payload, FutureCallback<Result<T>> callback, ResponseParser<T> parser) {
    return sendRequest(endpoint, payload, "DELETE", null, callback, parser);
  }

  protected Future<Result<T>> sendHeadRequest(String endpoint, Class<T> classOfT, FutureCallback<Result<T>> callback) {
    return sendRequest(endpoint, null, "HEAD", classOfT, callback, null);
  }

  protected Future<Result<T>> sendHeadRequest(String endpoint, FutureCallback<Result<T>> callback, ResponseParser<T> parser) {
    return sendRequest(endpoint, null, "HEAD", null, callback, parser);
  }

  private Future<Result<T>> sendRequest(String endpoint, Object payload, String method, Class<T> classOfT, FutureCallback<Result<T>> callback, ResponseParser<T> parser) {
    return client.execute(SimpleRequestProducer.create(getRequest(endpoint, payload, method)), new WeaviateResponseConsumer<>(classOfT, parser), callback);
  }

  protected SimpleHttpRequest getRequest(String endpoint, Object payload, String method) {
    SimpleHttpRequest req = new SimpleHttpRequest(method, String.format("%s%s", config.getBaseURL(), endpoint));
    req.addHeader(HttpHeaders.ACCEPT, "*/*");
    req.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    if (payload != null) {
      req.setBody(serializer.toJsonString(payload), ContentType.APPLICATION_JSON);
    }
    return req;
  }
}
