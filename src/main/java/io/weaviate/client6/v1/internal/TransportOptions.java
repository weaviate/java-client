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
  protected final Timeout timeout;

  protected TransportOptions(String scheme, String host, int port, H headers, TokenProvider tokenProvider,
      TrustManagerFactory tmf, Timeout timeout) {
    this.scheme = scheme;
    this.host = host;
    this.port = port;
    this.tokenProvider = tokenProvider;
    this.headers = headers;
    this.timeout = timeout;
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
  public Timeout timeout() {
    return this.timeout;
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

  /**
   * isWeaviateDomain returns true if the host matches weaviate.io,
   * semi.technology, or weaviate.cloud domain.
   */
  public static boolean isWeaviateDomain(String host) {
    var lower = host.toLowerCase();
    return lower.contains("weaviate.io") ||
        lower.contains("semi.technology") ||
        lower.contains("weaviate.cloud");
  }

  public static boolean isGoogleCloudDomain(String host) {
    var lower = host.toLowerCase();
    return lower.contains("gcp");
  }
}
