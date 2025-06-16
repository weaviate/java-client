package io.weaviate.client6.v1.internal;

public abstract class TransportOptions<H> {
  private final String scheme;
  private final String host;
  private final int port;
  private final TokenProvider tokenProvider;
  private final H headers;

  protected TransportOptions(String scheme, String host, int port, H headers, TokenProvider tokenProvider) {
    this.scheme = scheme;
    this.host = host;
    this.port = port;
    this.tokenProvider = tokenProvider;
    this.headers = headers;
  }

  public boolean isSecure() {
    return scheme == "https";
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

  public TokenProvider tokenProvider() {
    return this.tokenProvider;
  }

  public H headers() {
    return this.headers;
  }
}
