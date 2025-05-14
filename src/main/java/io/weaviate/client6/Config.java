package io.weaviate.client6;

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;

import io.weaviate.client6.v1.internal.grpc.GrpcChannelOptions;

public class Config implements GrpcChannelOptions {
  private final String version = "v1";
  private final String scheme;
  private final String httpHost;
  private final String grpcHost;
  private final Collection<Entry<String, String>> headers = Collections.emptyList();

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
    return grpcHost;
  }

  @Override
  public Collection<Entry<String, String>> headers() {
    return headers;
  }

  @Override
  public boolean useTls() {
    return scheme.equals("https");
  }
}
