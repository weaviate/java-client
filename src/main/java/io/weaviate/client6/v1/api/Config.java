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

  public static Config of(String scheme, Function<Custom, ObjectBuilder<Config>> fn) {
    return fn.apply(new Custom(scheme)).build();
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
    // Required parameters;
    protected final String scheme;

    protected String httpHost;
    protected int httpPort;
    protected String grpcHost;
    protected int grpcPort;
    protected TokenProvider tokenProvider;
    protected Map<String, String> headers = new HashMap<>();

    protected Builder(String scheme) {
      this.scheme = scheme;
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

    @Override
    public Config build() {
      return new Config(this);
    }
  }

  public static class Local extends Builder<Local> {
    public Local() {
      super("http");
      host("localhost");
      httpPort(8080);
      grpcPort(50051);
    }

    public Local host(String host) {
      this.httpHost = host;
      this.grpcHost = host;
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
    public WeaviateCloud(String clusterUrl, TokenProvider tokenProvider) {
      this(URI.create(clusterUrl), tokenProvider);
    }

    public WeaviateCloud(URI clusterUrl, TokenProvider tokenProvider) {
      super("https");
      this.httpHost = clusterUrl.getHost();
      this.httpPort = 443;
      this.grpcHost = "grpc-" + httpPort;
      this.grpcPort = 443;
      this.tokenProvider = tokenProvider;
    }
  }

  public static class Custom extends Builder<Custom> {
    public Custom(String scheme) {
      super(scheme);
      this.httpPort = scheme == "https" ? 443 : 80;
      this.grpcPort = scheme == "https" ? 443 : 80;
    }

    public Custom httpHost(String host) {
      this.httpHost = host;
      return this;
    }

    public Custom httpPort(int port) {
      this.grpcPort = port;
      return this;
    }

    public Custom grpcHost(String host) {
      this.grpcHost = host;
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
