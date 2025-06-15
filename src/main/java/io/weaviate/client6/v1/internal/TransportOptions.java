package io.weaviate.client6.v1.internal;

import java.util.Map;

public abstract class TransportOptions<H> {
  private final String host;
  private final TokenProvider tokenProvider;
  private final H headers;

  protected TransportOptions(String host, H headers, TokenProvider tokenProvider) {
    this.host = host;
    this.tokenProvider = tokenProvider;
    this.headers = headers;
  }

  public String host() {
    return this.host;
  }

  public TokenProvider tokenProvider() {
    return this.tokenProvider;
  }

  public H headers() {
    return this.headers;
  }
}
