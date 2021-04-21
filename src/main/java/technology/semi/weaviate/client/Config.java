package technology.semi.weaviate.client;

import java.util.Map;

public class Config {
  private final String scheme;
  private final String host;
  private final String version;
  private final Map<String, String> headers;

  public Config(String scheme, String host, Map<String, String> headers) {
    this.scheme = scheme;
    this.host = host;
    this.version = "v1";
    this.headers = headers;
  }

  public Config(String scheme, String host) {
    this(scheme, host, null);
  }

  public String getBaseURL() {
    return scheme + "://" + host + "/" + version;
  }

  public Map<String, String> getHeaders() {
    return this.headers;
  }
}
