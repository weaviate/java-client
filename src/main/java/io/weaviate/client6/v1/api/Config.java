package io.weaviate.client6.v1.api;

import java.util.Collections;
import java.util.Map;

import io.weaviate.client6.v1.internal.grpc.GrpcChannelOptions;
import io.weaviate.client6.v1.internal.rest.TransportOptions;

public class Config {
  private final String version = "v1";
  private final String scheme;
  private final String httpHost;
  private final String grpcHost;
  private final Map<String, String> headers = Collections.emptyMap();

  public Config(String scheme, String httpHost, String grpcHost) {
    this.scheme = scheme;
    this.httpHost = httpHost;
    this.grpcHost = grpcHost;
  }

  public String baseUrl() {
    return scheme + "://" + httpHost + "/" + version;
  }

  public String grpcAddress() {
    if (grpcHost.contains(":")) {
      return grpcHost;
    }
    // FIXME: use secure port (433) if scheme == https
    return String.format("%s:80", grpcHost);
  }

  public TransportOptions rest() {
    return new TransportOptions() {

      @Override
      public String host() {
        return baseUrl();
      }

      @Override
      public Map<String, String> headers() {
        return headers;
      }

    };
  }

  public GrpcChannelOptions grpc() {
    return new GrpcChannelOptions() {
      @Override
      public String host() {
        return grpcAddress();
      }

      @Override
      public boolean useTls() {
        return scheme.equals("https");
      }

      @Override
      public Map<String, String> headers() {
        return headers;
      }
    };
  }
}
