package io.weaviate.client6.v1.internal.rest;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.io.CloseMode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DefaultRestTransport implements RestTransport {
  private final CloseableHttpClient httpClient;
  private final CloseableHttpAsyncClient httpClientAsync;
  private final TransportOptions transportOptions;

  private static final Gson gson = new GsonBuilder().create();

  public DefaultRestTransport(TransportOptions options) {
    this.transportOptions = options;
    this.httpClient = HttpClients.createDefault();
    this.httpClientAsync = HttpAsyncClients.createDefault();
    httpClientAsync.start();
  }

  @Override
  public <RequestT, ResponseT> ResponseT performRequest(RequestT request, Endpoint<RequestT, ResponseT> endpoint)
      throws IOException {
    var req = prepareClassicRequest(request, endpoint);
    return this.httpClient.execute(req, response -> endpoint.deserializeResponse(gson, response));
  }

  @Override
  public <RequestT, ResponseT> CompletableFuture<ResponseT> performRequestAsync(RequestT request,
      Endpoint<RequestT, ResponseT> endpoint) {
    var req = prepareSimpleRequest(request, endpoint);

    var completable = new CompletableFuture<>();
    this.httpClientAsync.execute(req, new FutureCallback<>() {

      @Override
      public void completed(SimpleHttpResponse result) {
        completable.complete(result);
      }

      @Override
      public void failed(Exception ex) {
        completable.completeExceptionally(ex);
      }

      @Override
      public void cancelled() {
        completable.cancel(false);
      }

    });
    return completable.thenApply(r -> endpoint.deserializeResponse(gson, (SimpleHttpResponse) r));
  }

  private <RequestT> SimpleHttpRequest prepareSimpleRequest(RequestT request, Endpoint<RequestT, ?> endpoint) {
    var method = endpoint.method(request);
    var uri = transportOptions.host() + endpoint.requestUrl(request);
    // TODO: apply options;

    var body = endpoint.body(gson, request);
    var req = SimpleHttpRequest.create(method, uri);
    req.setBody(body.getBytes(), ContentType.APPLICATION_JSON);
    return req;
  }

  private <RequestT> ClassicHttpRequest prepareClassicRequest(RequestT request, Endpoint<RequestT, ?> endpoint) {
    var method = endpoint.method(request);
    var uri = transportOptions.host() + endpoint.requestUrl(request);
    // TODO: apply options;
    var body = endpoint.body(gson, request);
    return ClassicRequestBuilder.create(method)
        .setEntity(body, ContentType.APPLICATION_JSON)
        .setUri(uri)
        .build();
  }

  @Override
  public void close() throws IOException {
    httpClient.close();
    httpClientAsync.close(CloseMode.GRACEFUL);
  }
}
