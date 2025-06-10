package io.weaviate.client6;

import java.util.Collections;
import java.util.Map;

import io.weaviate.client6.v1.internal.grpc.GrpcChannelOptions;

public class Config implements GrpcChannelOptions {
  public final String version = "v1";
  public final String scheme;
  public final String httpHost;
  public final String grpcHost;
  public final Map<String, String> headers = Collections.emptyMap();

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

  // GrpcChannelOptions -------------------------------------------------------

  @Override
  public String host() {
    return grpcAddress();
  }

  @Override
  public Map<String, String> headers() {
    return headers;
  }

  @Override
  public boolean useTls() {
    return scheme.equals("https");
  }
}
