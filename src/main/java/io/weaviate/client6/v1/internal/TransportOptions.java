package io.weaviate.client6.v1.internal;

import java.util.Map;

public abstract class TransportOptions<H> {
  private final String host;
  private final TokenProvider tokenProvider;
  private final H headers;

  protected TransportOptions(String host, Map<String, String> headers, TokenProvider tokenProvider) {
    this.host = host;
    this.tokenProvider = tokenProvider;
    this.headers = buildHeaders(headers);
  }

  protected abstract H buildHeaders(Map<String, String> headers);

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
