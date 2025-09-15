package io.weaviate.client6.v1.api;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.net.ssl.TrustManagerFactory;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.grpc.GrpcChannelOptions;
import io.weaviate.client6.v1.internal.rest.RestTransportOptions;

public record Config(
    String scheme,
    String httpHost,
    int httpPort,
    String grpcHost,
    int grpcPort,
    Map<String, String> headers,
    Authentication authentication,
    TrustManagerFactory trustManagerFactory) {

  public static Config of(Function<Custom, ObjectBuilder<Config>> fn) {
    return fn.apply(new Custom()).build();
  }

  private Config(Builder<?> builder) {
    this(
        builder.scheme,
        builder.httpHost,
        builder.httpPort,
        builder.grpcHost,
        builder.grpcPort,
        builder.headers,
        builder.authentication,
        builder.trustManagerFactory);
  }

  RestTransportOptions restTransportOptions() {
    return restTransportOptions(null);
  }

  RestTransportOptions restTransportOptions(TokenProvider tokenProvider) {
    return new RestTransportOptions(scheme, httpHost, httpPort, headers, tokenProvider, trustManagerFactory);
  }

  GrpcChannelOptions grpcTransportOptions() {
    return grpcTransportOptions(null);
  }

  GrpcChannelOptions grpcTransportOptions(TokenProvider tokenProvider) {
    return new GrpcChannelOptions(scheme, grpcHost, grpcPort, headers, tokenProvider, trustManagerFactory);
  }

  private abstract static class Builder<SELF extends Builder<SELF>> implements ObjectBuilder<Config> {
    protected String scheme;

    protected String httpHost;
    protected int httpPort;
    protected String grpcHost;
    protected int grpcPort;
    protected Authentication authentication;
    protected TrustManagerFactory trustManagerFactory;
    protected Map<String, String> headers = new HashMap<>();

    /**
     * Set URL scheme. Subclasses may increase the visibility of this method to
     * {@code public} if using a different scheme is allowed.
     */
    @SuppressWarnings("unchecked")
    protected SELF scheme(String scheme) {
      this.scheme = scheme;
      return (SELF) this;
    }

    /**
     * Set port for REST requests. Subclasses may increase the visibility of this
     * method to {@code public} if using a different port is allowed.
     */
    @SuppressWarnings("unchecked")
    protected SELF httpHost(String httpHost) {
      this.httpHost = trimScheme(httpHost);
      return (SELF) this;
    }

    /**
     * Set port for gRPC requests. Subclasses may increase the visibility of this
     * method to {@code public} if using a different port is allowed.
     */
    @SuppressWarnings("unchecked")
    protected SELF grpcHost(String grpcHost) {
      this.grpcHost = trimScheme(grpcHost);
      return (SELF) this;
    }

    /** Remove leading http(s):// prefix from a URL, if present. */
    private String trimScheme(String url) {
      return url.replaceFirst("^https?\\/\\/", "");
    }

    /**
     * Provide a {@link TrustManagerFactory}. Subclasses which support
     * secure connection should expose this method.
     */
    @SuppressWarnings("unchecked")
    protected SELF trustManagerFactory(TrustManagerFactory tmf) {
      this.trustManagerFactory = tmf;
      return (SELF) this;
    }

    /**
     * Set authentication method. Setting this to {@code null} or omitting
     * will not use any authentication mechanism.
     */
    @SuppressWarnings("unchecked")
    public SELF authentication(Authentication authz) {
      this.authentication = authz;
      return (SELF) this;
    }

    /**
     * Set a single request header. The client does not support header lists,
     * so there is no equivalent {@code addHeader} to append to existing header.
     * This will be applied both to REST and gRPC requests.
     */
    @SuppressWarnings("unchecked")
    public SELF setHeader(String key, String value) {
      this.headers.put(key, value);
      return (SELF) this;
    }

    /**
     * Set multiple request headers.
     * This will be applied both to REST and gRPC requests.
     */
    @SuppressWarnings("unchecked")
    public SELF setHeaders(Map<String, String> headers) {
      this.headers.putAll(Map.copyOf(headers));
      return (SELF) this;
    }

    /**
     * Weaviate will use the URL in this header to call Weaviate Embeddings
     * Service if an appropriate vectorizer is configured for collection.
     */
    private static final String HEADER_X_WEAVIATE_CLUSTER_URL = "X-Weaviate-Cluster-URL";

    /**
     * isWeaviateDomain returns true if the host matches weaviate.io,
     * semi.technology, or weaviate.cloud domain.
     */
    private static boolean isWeaviateDomain(String host) {
      var lower = host.toLowerCase();
      return lower.contains("weaviate.io") ||
          lower.contains("semi.technology") ||
          lower.contains("weaviate.cloud");
    }

    @Override
    public Config build() {
      // For clusters hosted on Weaviate Cloud, Weaviate Embedding Service
      // will be available under the same domain.
      if (isWeaviateDomain(httpHost) && authentication != null) {
        setHeader(HEADER_X_WEAVIATE_CLUSTER_URL, "https://" + httpHost + ":" + httpPort);
      }
      return new Config(this);
    }
  }

  /**
   * Configuration for Weaviate instances deployed locally.
   *
   * <p>
   * Has sane defaults that match standard Weaviate deployment configuration:
   * <ul>
   * <li>{@code scheme: http}</li>
   * <li>{@code host: localhost}</li>
   * <li>{@code httpPort: 8080}</li>
   * <li>{@code grpcPort: 50051}</li>
   * </ul>
   */
  public static class Local extends Builder<Local> {
    public Local() {
      scheme("http");
      host("localhost");
      port(8080);
      grpcPort(50051);
    }

    /**
     * Set a different hostname.
     * This changes both {@code httpHost} and {@code grpcHost}.
     */
    public Local host(String host) {
      httpHost(host);
      grpcHost(host);
      return this;
    }

    /** Override default HTTP port. */
    public Local port(int port) {
      this.httpPort = port;
      return this;
    }

    /** Override default gRPC port. */
    public Local grpcPort(int port) {
      this.grpcPort = port;
      return this;
    }
  }

  /**
   * Configuration for instances hosted on Weaviate Cloud.
   * {@link WeaviateCloud} will create a secure client
   * with {@code schema: https} and {@code http-/grpcPort: 443}.
   *
   * Custom SSL certificates are suppored via
   * {@link #trustManagerFactory}.
   */
  public static class WeaviateCloud extends Builder<WeaviateCloud> {
    public WeaviateCloud(String httpHost, Authentication authentication) {
      this(URI.create(httpHost), authentication);
    }

    public WeaviateCloud(URI clusterUri, Authentication authentication) {
      scheme("https");
      super.httpHost(clusterUri.getHost() != null
          ? clusterUri.getHost() // https://[example.com]/about
          : clusterUri.getPath().split("/")[0]); // [example.com]/about
      super.grpcHost("grpc-" + this.httpHost);
      this.httpPort = 443;
      this.grpcPort = 443;
      this.authentication = authentication;
    }

    /**
     * Configure a custom TrustStore to validate third-party SSL certificates.
     *
     * <p>
     * Usage:
     *
     * <pre>{@code
     * // Create a TrustManagerFactory to validate custom certificates.
     * TrustManagerFactory tmf;
     * try (var keys = new FileInputStream("/path/to/custom/truststore.p12")) {
     *   KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
     *   trustStore.load(myKeys, "secret-password".toCharArra());
     *
     *   tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
     *   tmf.init(trustStore);
     * }
     *
     * // Pass it to wcd -> wcd.trustManagerFactory(tmf)
     * }</pre>
     */
    public WeaviateCloud trustManagerFactory(TrustManagerFactory tmf) {
      return super.trustManagerFactory(tmf);
    }
  }

  /** Configuration for custom Weaviate deployements. */
  public static class Custom extends Builder<Custom> {
    /**
     * Scheme controls which protocol will be used for the database connection.
     * REST and gRPC ports will be automatically inferred from it:
     * <strong>443</strong> for HTTPS connection and <strong>80</strong> for HTTP.
     *
     * These can be overriden with {@link #httpPort(int)} and
     * {@link #grpcPort(int)}.
     */
    public Custom scheme(String scheme) {
      httpPort("https".equals(scheme) ? 443 : 80);
      grpcPort("https".equals(scheme) ? 443 : 80);
      return super.scheme(scheme);
    }

    /** Set HTTP hostname. */
    public Custom httpHost(String httpHost) {
      super.httpHost(httpHost);
      return this;
    }

    /** Set HTTP port. */
    public Custom httpPort(int port) {
      this.httpPort = port;
      return this;
    }

    /** Set gRPC hostname. */
    public Custom grpcHost(String grpcHost) {
      super.grpcHost(grpcHost);
      return this;
    }

    /** Set gRPC port. */
    public Custom grpcPort(int port) {
      this.grpcPort = port;
      return this;
    }

    /**
     * Configure a custom TrustStore to validate third-party SSL certificates.
     *
     * <p>
     * Usage:
     *
     * <pre>{@code
     * // Create a TrustManagerFactory to validate custom certificates.
     * TrustManagerFactory tmf;
     * try (var keys = new FileInputStream("/path/to/custom/truststore.p12")) {
     *   KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
     *   trustStore.load(myKeys, "secret-password".toCharArra());
     *
     *   tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
     *   tmf.init(trustStore);
     * }
     *
     * // Pass it to custom -> custom.trustManagerFactory(tmf)
     * }</pre>
     */
    public Custom trustManagerFactory(TrustManagerFactory tmf) {
      return super.trustManagerFactory(tmf);
    }
  }
}
