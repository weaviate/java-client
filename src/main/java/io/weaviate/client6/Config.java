package io.weaviate.client6;

public class Config {
  private final String version = "v1";
  private final String scheme;
  private final String host;

  public Config(String scheme, String host) {
    this.scheme = scheme;
    this.host = host;
  }

  public String baseUrl() {
    return scheme + "://" + host + "/" + version;
  }
}
