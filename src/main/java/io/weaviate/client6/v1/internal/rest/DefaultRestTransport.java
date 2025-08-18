package io.weaviate.client6.v1.internal.rest;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.io.CloseMode;

import io.weaviate.client6.v1.api.WeaviateApiException;

public class DefaultRestTransport implements RestTransport {
  private final CloseableHttpClient httpClient;
  private final CloseableHttpAsyncClient httpClientAsync;
  private final RestTransportOptions transportOptions;

  private AuthenticationInterceptor authInterceptor;

  public DefaultRestTransport(RestTransportOptions transportOptions) {
    this.transportOptions = transportOptions;

    // TODO: doesn't make sense to spin up both?
    var httpClient = HttpClients.custom()
        .setDefaultHeaders(transportOptions.headers());
    var httpClientAsync = HttpAsyncClients.custom()
        .setDefaultHeaders(transportOptions.headers());

    // Apply custom SSL context
    if (transportOptions.trustManagerFactory() != null) {
      DefaultClientTlsStrategy tlsStrategy;
      try {
        var sslCtx = SSLContext.getInstance("TLS");
        sslCtx.init(null, transportOptions.trustManagerFactory().getTrustManagers(), null);
        tlsStrategy = new DefaultClientTlsStrategy(sslCtx);
      } catch (NoSuchAlgorithmException | KeyManagementException e) {
        // todo: throw WeaviateConnectionException
        throw new RuntimeException("connect to Weaviate", e);
      }

      PoolingHttpClientConnectionManager syncManager = PoolingHttpClientConnectionManagerBuilder.create()
          .setTlsSocketStrategy(tlsStrategy).build();
      PoolingAsyncClientConnectionManager asyncManager = PoolingAsyncClientConnectionManagerBuilder.create()
          .setTlsStrategy(tlsStrategy).build();

      httpClient.setConnectionManager(syncManager);
      httpClientAsync.setConnectionManager(asyncManager);
    }

    if (transportOptions.tokenProvider() != null) {
      this.authInterceptor = new AuthenticationInterceptor(transportOptions.tokenProvider());
      httpClient.addRequestInterceptorFirst(authInterceptor);
      httpClientAsync.addExecInterceptorFirst("auth", authInterceptor);
    }

    this.httpClient = httpClient.build();
    this.httpClientAsync = httpClientAsync.build();
    this.httpClientAsync.start();
  }

  @Override
  public <RequestT, ResponseT, ExceptionT> ResponseT performRequest(RequestT request,
      Endpoint<RequestT, ResponseT> endpoint)
      throws IOException {

    var req = prepareClassicRequest(request, endpoint);
    return this.httpClient.execute(req, r -> this.handleResponse(endpoint, req.getMethod(), req.getRequestUri(), r));
  }

  private <RequestT, ResponseT> ClassicHttpRequest prepareClassicRequest(RequestT request,
      Endpoint<RequestT, ResponseT> endpoint) {
    var method = endpoint.method(request);
    var uri = endpoint.requestUrl(transportOptions, request);

    // TODO: apply options;
    var req = ClassicRequestBuilder.create(method).setUri(uri);
    var body = endpoint.body(request);
    if (body != null) {
      req.setEntity(body, ContentType.APPLICATION_JSON);
    }
    return req.build();
  }

  private <ResponseT, ExceptionT> ResponseT handleResponse(Endpoint<?, ResponseT> endpoint, String method, String url,
      ClassicHttpResponse httpResponse) throws IOException, ParseException {
    var statusCode = httpResponse.getCode();
    var body = httpResponse.getEntity() != null
        ? EntityUtils.toString(httpResponse.getEntity())
        : "";
    return _handleResponse(endpoint, method, url, statusCode, body);
  }

  @Override
  public <RequestT, ResponseT, ExceptionT> CompletableFuture<ResponseT> performRequestAsync(RequestT request,
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
    return completable
        .thenApply(r -> (ResponseT) handleResponseAsync(endpoint,
            req.getMethod(), req.getRequestUri(), r));
  }

  private <RequestT, ResponseT> SimpleHttpRequest prepareSimpleRequest(RequestT request,
      Endpoint<RequestT, ResponseT> endpoint) {
    var method = endpoint.method(request);
    var uri = endpoint.requestUrl(transportOptions, request);

    // TODO: apply options;

    var body = endpoint.body(request);
    var req = SimpleHttpRequest.create(method, uri);
    if (body != null) {
      req.setBody(body.getBytes(), ContentType.APPLICATION_JSON);
    }
    return req;
  }

  private <ResponseT, ExceptionT> ResponseT handleResponseAsync(
      Endpoint<?, ResponseT> endpoint,
      String method, String url,
      SimpleHttpResponse httpResponse) {
    var statusCode = httpResponse.getCode();
    var body = httpResponse.getBody() != null
        ? httpResponse.getBody().getBodyText()
        : "";
    return _handleResponse(endpoint, method, url, statusCode, body);
  }

  private <ResponseT> ResponseT _handleResponse(Endpoint<?, ResponseT> endpoint, String method, String url,
      int statusCode, String body) {
    if (endpoint.isError(statusCode)) {
      var message = endpoint.deserializeError(statusCode, body);
      throw WeaviateApiException.http(method, url, statusCode, message);
    }

    if (endpoint instanceof JsonEndpoint json) {
      @SuppressWarnings("unchecked")
      ResponseT response = (ResponseT) json.deserializeResponse(statusCode, body);
      return response;
    } else if (endpoint instanceof BooleanEndpoint bool) {
      @SuppressWarnings("unchecked")
      ResponseT response = (ResponseT) ((Boolean) bool.getResult(statusCode));
      return response;
    }

    // TODO: make it a WeaviateTransportException
    throw new RuntimeException("Unhandled endpoint type " + endpoint.getClass().getSimpleName());
  }

  @Override
  public RestTransportOptions getTransportOptions() {
    return transportOptions;
  }

  @Override
  public void close() throws Exception {
    httpClient.close();
    httpClientAsync.close(CloseMode.GRACEFUL);
    if (authInterceptor != null) {
      authInterceptor.close();
    }
  }
}
