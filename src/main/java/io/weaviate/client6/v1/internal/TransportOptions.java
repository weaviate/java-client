package io.weaviate.client6.v1.internal;

import javax.annotation.Nullable;
import javax.net.ssl.TrustManagerFactory;

public abstract class TransportOptions<H> {
  protected final String scheme;
  protected final String host;
  protected final int port;
  protected final TokenProvider tokenProvider;
  protected final H headers;
  protected final TrustManagerFactory trustManagerFactory;

  protected TransportOptions(String scheme, String host, int port, H headers, TokenProvider tokenProvider,
      TrustManagerFactory tmf) {
    this.scheme = scheme;
    this.host = host;
    this.port = port;
    this.tokenProvider = tokenProvider;
    this.headers = headers;
    this.trustManagerFactory = tmf;
  }

  public boolean isSecure() {
    return scheme.equals("https");
  }

  public String scheme() {
    return this.scheme;
  }

  public String host() {
    return this.host;
  }

  public int port() {
    return this.port;
  }

  @Nullable
  public TokenProvider tokenProvider() {
    return this.tokenProvider;
  }

  public H headers() {
    return this.headers;
  }

  @Nullable
  public TrustManagerFactory trustManagerFactory() {
    return this.trustManagerFactory;
  }
}
