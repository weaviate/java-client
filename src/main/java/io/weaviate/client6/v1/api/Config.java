package io.weaviate.client6.v1.api;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.TokenProvider;
import io.weaviate.client6.v1.internal.grpc.GrpcChannelOptions;
import io.weaviate.client6.v1.internal.rest.RestTransportOptions;

public class Config {
  private final String version = "v1";
  private final String scheme;
  private final String httpHost;
  private final String grpcHost;
  private final Map<String, String> headers;
  private final TokenProvider tokenProvider;

  public Config(
      String scheme,
      String httpHost,
      String grpcHost,
      Map<String, String> headers,
      TokenProvider tokenProvider) {
    this.scheme = scheme;
    this.httpHost = httpHost;
    this.grpcHost = grpcHost;
    this.headers = headers;
    this.tokenProvider = tokenProvider;
  }

  public Config(String scheme, String httpHost, String grpcHost) {
    this.scheme = scheme;
    this.httpHost = httpHost;
    this.grpcHost = grpcHost;
    this.headers = new HashMap<>();
    this.tokenProvider = null;
  }

  public static Config of(String scheme, String httpHost, Function<Config.Builder, ObjectBuilder<Config>> fn) {
    return fn.apply(new Builder(scheme, httpHost)).build();
  }

  protected String baseUrl(String hostname) {
    return scheme + "://" + hostname + "/" + version;
  }

  public RestTransportOptions restTransportOptions() {
    return new RestTransportOptions(baseUrl(httpHost), headers, tokenProvider);
  }

  public GrpcChannelOptions grpcTransportOptions() {
    return new GrpcChannelOptions(baseUrl(grpcHost), headers, tokenProvider);
  }

  public static class Builder implements ObjectBuilder<Config> {
    // Required parameters
    private final String scheme;
    private final String httpHost;

    public Builder(String url) {
      this(URI.create(url));
    }

    public Builder(URI url) {
      this(url.getScheme(), url.getHost());
    }

    public Builder(String scheme, String httpHost) {
      this.scheme = scheme;
      this.httpHost = httpHost;
    }

    private String grpcPrefix;
    private String grpcHost;
    private TokenProvider tokenProvider;

    private Map<String, String> headers = new HashMap<>();

    public Builder grpcPrefix(String prefix) {
      this.grpcPrefix = prefix;
      return this;
    }

    public Builder grpcHost(String host) {
      this.grpcHost = host;
      return this;
    }

    public Builder authorization(TokenProvider tokenProvider) {
      this.tokenProvider = tokenProvider;
      return this;
    }

    public Builder setHeader(String key, String value) {
      this.headers.put(key, value);
      return this;
    }

    public Builder setHeaders(Map<String, String> headers) {
      this.headers = Map.copyOf(headers);
      return this;
    }

    @Override
    public Config build() {
      if (grpcHost == null && grpcPrefix == null) {
        throw new RuntimeException("grpcHost and grpcPrefix cannot both be null");
      }

      return new Config(
          scheme,
          httpHost,
          grpcHost != null ? grpcHost : grpcPrefix + httpHost,
          headers,
          tokenProvider);
    }
  }
}
