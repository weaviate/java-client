package io.weaviate.client6.v1.internal.rest;

import java.io.IOException;

import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecChain.Scope;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.protocol.HttpContext;

import io.weaviate.client6.v1.internal.AsyncTokenProvider;
import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.TokenProvider.Token;

/**
 * AuthenticationInterceptor can supply Authorization headers to both
 * synchronous and asynchronous Apache HttpClient.
 */
class AuthenticationInterceptor implements HttpRequestInterceptor, AsyncExecChainHandler, AutoCloseable {
  private static final String AUTHORIZATION = "Authorization";

  private final TokenProvider tokenProvider;
  private final AsyncTokenProvider tokenProviderAsync;

  AuthenticationInterceptor(TokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
    this.tokenProviderAsync = AsyncTokenProvider.wrap(tokenProvider);
  }

  /**
   * Add Authorization header to a blocking request.
   * See {@link HttpRequestInterceptor}.
   */
  @Override
  public void process(HttpRequest request, EntityDetails entity, HttpContext context)
      throws HttpException, IOException {
    var token = tokenProvider.getToken();
    setAuthorization(request, token);
  }

  /**
   * Add Authorization header to a non-blocking request.
   * See {@link AsyncExecChainHandler}.
   */
  @Override
  public void execute(HttpRequest request, AsyncEntityProducer entityProducer, Scope scope, AsyncExecChain chain,
      AsyncExecCallback callback) throws HttpException, IOException {

    tokenProviderAsync.getToken().whenComplete((tok, error) -> {
      if (error != null) {
        callback.failed(error instanceof Exception ex ? ex : new RuntimeException(error));
        return;
      }

      setAuthorization(request, tok);

      try {
        chain.proceed(request, entityProducer, scope, callback);
      } catch (HttpException | IOException e) {
        callback.failed(e);
      }
    });
  }

  private void setAuthorization(HttpRequest request, Token token) {
    request.addHeader(new BasicHeader(AUTHORIZATION, "Bearer " + token.accessToken()));
  }

  @Override
  public void close() throws Exception {
    tokenProviderAsync.close();
  }
}
