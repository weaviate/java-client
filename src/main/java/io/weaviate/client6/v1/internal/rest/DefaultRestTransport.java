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
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.io.CloseMode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DefaultRestTransport implements RestTransport {
  private final CloseableHttpClient httpClient;
  private final CloseableHttpAsyncClient httpClientAsync;
  private final RestTransportOptions transportOptions;

  // TODO: retire
  private static final Gson gson = new GsonBuilder().create();

  public DefaultRestTransport(RestTransportOptions transportOptions) {
    this.transportOptions = transportOptions;

    // TODO: doesn't make sense to spin up both?
    var httpClient = HttpClients.custom()
        .setDefaultHeaders(transportOptions.headers());
    var httpClientAsync = HttpAsyncClients.custom()
        .setDefaultHeaders(transportOptions.headers());

    if (transportOptions.tokenProvider() != null) {
      var interceptor = new AuthorizationInterceptor(transportOptions.tokenProvider());
      httpClient.addRequestInterceptorFirst(interceptor);
      httpClientAsync.addRequestInterceptorFirst(interceptor);
    }

    this.httpClient = httpClient.build();
    this.httpClientAsync = httpClientAsync.build();
    this.httpClientAsync.start();
  }

  @Override
  public <RequestT, ResponseT> ResponseT performRequest(RequestT request, Endpoint<RequestT, ResponseT> endpoint)
      throws IOException {
    var req = prepareClassicRequest(request, endpoint);
    // FIXME: we need to differentiate between "no body" and "soumething's wrong"
    return this.httpClient.execute(req,
        response -> response.getEntity() != null
            ? endpoint.deserializeResponse(gson, EntityUtils.toString(response.getEntity()))
            : null);
  }

  @Override
  public <RequestT, ResponseT> CompletableFuture<ResponseT> performRequestAsync(RequestT request,
      Endpoint<RequestT, ResponseT> endpoint) {
    var req = prepareSimpleRequest(request, endpoint);

    var completable = new CompletableFuture<SimpleHttpResponse>();
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
    // FIXME: we need to differentiate between "no body" and "soumething's wrong"
    return completable.thenApply(r -> r.getBody() == null
        ? endpoint.deserializeResponse(gson, r.getBody().getBodyText())
        : null);
  }

  private <RequestT> SimpleHttpRequest prepareSimpleRequest(RequestT request, Endpoint<RequestT, ?> endpoint) {
    var method = endpoint.method(request);
    var uri = transportOptions.baseUrl() + endpoint.requestUrl(request);
    // TODO: apply options;

    var body = endpoint.body(gson, request);
    var req = SimpleHttpRequest.create(method, uri);
    if (body != null) {
      req.setBody(body.getBytes(), ContentType.APPLICATION_JSON);
    }
    return req;
  }

  private <RequestT> ClassicHttpRequest prepareClassicRequest(RequestT request, Endpoint<RequestT, ?> endpoint) {
    var method = endpoint.method(request);
    var uri = transportOptions.baseUrl() + endpoint.requestUrl(request);

    // TODO: apply options;
    var req = ClassicRequestBuilder.create(method).setUri(uri);
    var body = endpoint.body(gson, request);
    if (body != null) {
      req.setEntity(body, ContentType.APPLICATION_JSON);
    }
    return req.build();
  }

  @Override
  public void close() throws IOException {
    httpClient.close();
    httpClientAsync.close(CloseMode.GRACEFUL);
  }
}
