package io.weaviate.client.base;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.async.WeaviateResponseConsumer;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;

public abstract class AsyncBaseClient<T> {
  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final Serializer serializer;

  public AsyncBaseClient(CloseableHttpAsyncClient client, Config config) {
    this.client = client;
    this.config = config;
    this.serializer = new Serializer();
  }

  protected Future<Result<T>> sendGetRequest(String endpoint, Class<T> classOfT, FutureCallback<Result<T>> callback) {
    return sendRequest(endpoint, null, "GET", classOfT, callback);
  }

  private Future<Result<T>> sendRequest(String endpoint, Object payload, String method, Class<T> classOfT, FutureCallback<Result<T>> callback) {
    SimpleHttpRequest req = new SimpleHttpRequest(method, String.format("%s%s", config.getBaseURL(), endpoint));
    req.addHeader(HttpHeaders.ACCEPT, "*/*");
    req.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    if (payload != null) {
      req.setBody(serializer.toJsonString(payload), ContentType.APPLICATION_JSON);
    }
    return client.execute(SimpleRequestProducer.create(req), new WeaviateResponseConsumer<>(classOfT), callback);
  }
}
