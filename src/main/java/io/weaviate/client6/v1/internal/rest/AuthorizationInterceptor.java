package io.weaviate.client6.v1.internal.rest;

import java.io.IOException;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.protocol.HttpContext;

import io.weaviate.client6.v1.internal.TokenProvider;

class AuthorizationInterceptor implements HttpRequestInterceptor {
  private static final String AUTHORIZATION = "Authorization";

  private final TokenProvider tokenProvider;

  AuthorizationInterceptor(TokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  public void process(HttpRequest request, EntityDetails entity, HttpContext context)
      throws HttpException, IOException {
    var token = tokenProvider.getToken().accessToken();
    request.addHeader(new BasicHeader(AUTHORIZATION, "Bearer " + token));
  }
}
