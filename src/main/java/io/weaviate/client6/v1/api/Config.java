package io.weaviate.client6.v1.api;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
    TokenProvider tokenProvider) {

  public static Config of(Function<Custom, ObjectBuilder<Config>> fn) {
    return fn.apply(new Custom()).build();
  }

  public Config(Builder<?> builder) {
    this(
        builder.scheme,
        builder.httpHost,
        builder.httpPort,
        builder.grpcHost,
        builder.grpcPort,
        builder.headers,
        builder.tokenProvider);
  }

  public RestTransportOptions restTransportOptions() {
    return new RestTransportOptions(scheme, httpHost, httpPort, headers, tokenProvider);
  }

  public GrpcChannelOptions grpcTransportOptions() {
    return new GrpcChannelOptions(scheme, grpcHost, grpcPort, headers, tokenProvider);
  }

  abstract static class Builder<SELF extends Builder<SELF>> implements ObjectBuilder<Config> {
    protected String scheme;

    protected String httpHost;
    protected int httpPort;
    protected String grpcHost;
    protected int grpcPort;
    protected TokenProvider tokenProvider;
    protected Map<String, String> headers = new HashMap<>();

    @SuppressWarnings("unchecked")
    protected SELF scheme(String scheme) {
      this.scheme = scheme;
      return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    protected SELF httpHost(String httpHost) {
      this.httpHost = trimScheme(httpHost);
      return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    protected SELF grpcHost(String grpcHost) {
      this.grpcHost = trimScheme(grpcHost);
      return (SELF) this;
    }

    /** Remove leading http(s):// prefix from a URL, if present. */
    private String trimScheme(String url) {
      return url.replaceFirst("^https?\\/\\/", "");
    }

    @SuppressWarnings("unchecked")
    public SELF setHeader(String key, String value) {
      this.headers.put(key, value);
      return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF setHeaders(Map<String, String> headers) {
      this.headers = Map.copyOf(headers);
      return (SELF) this;
    }

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
      if (isWeaviateDomain(httpHost) && tokenProvider != null) {
        setHeader(HEADER_X_WEAVIATE_CLUSTER_URL, "https://" + httpHost + ":" + httpPort);
      }
      return new Config(this);
    }
  }

  public static class Local extends Builder<Local> {
    public Local() {
      scheme("http");
      host("localhost");
      httpPort(8080);
      grpcPort(50051);
    }

    public Local host(String host) {
      httpHost(host);
      grpcHost(host);
      return this;
    }

    public Local httpPort(int port) {
      this.httpPort = port;
      return this;
    }

    public Local grpcPort(int port) {
      this.grpcPort = port;
      return this;
    }
  }

  public static class WeaviateCloud extends Builder<WeaviateCloud> {
    public WeaviateCloud(String httpHost, TokenProvider tokenProvider) {
      this(URI.create(httpHost), tokenProvider);
    }

    public WeaviateCloud(URI clusterUri, TokenProvider tokenProvider) {
      scheme("https");
      super.httpHost(clusterUri.getHost() != null
          ? clusterUri.getHost() // https://[example.com]/about
          : clusterUri.getPath().split("/")[0]); // [example.com]/about
      this.httpPort = 443;
      super.grpcHost("grpc-" + this.httpHost);
      this.grpcPort = 443;
      this.tokenProvider = tokenProvider;
    }
  }

  public static class Custom extends Builder<Custom> {
    /**
     * Scheme controls which protocol will be used for the database connection.
     * REST and gRPC ports will be automatically inferred from it:
     * <strong>443</strong> for HTTPS connection and <strong>80</strong> for HTTP.
     */
    public Custom scheme(String scheme) {
      httpPort(scheme == "https" ? 443 : 80);
      grpcPort(scheme == "https" ? 443 : 80);
      return super.scheme(scheme);
    }

    public Custom httpHost(String httpHost) {
      super.httpHost(httpHost);
      return this;
    }

    public Custom httpPort(int port) {
      this.grpcPort = port;
      return this;
    }

    public Custom grpcHost(String grpcHost) {
      super.grpcHost(grpcHost);
      return this;
    }

    public Custom grpcPort(int port) {
      this.grpcPort = port;
      return this;
    }

    public Custom authorization(TokenProvider tokenProvider) {
      this.tokenProvider = tokenProvider;
      return this;
    }
  }
}
