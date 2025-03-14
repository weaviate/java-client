package io.weaviate.client6;

public class Config {
  private final String version = "v1";
  private final String scheme;
  private final String httpHost;
  private final String grpcHost;

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
}
