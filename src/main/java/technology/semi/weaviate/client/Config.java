package technology.semi.weaviate.client;

import java.util.Map;

public class Config {

  private static final int DEFAULT_TIMEOUT_SECONDS = 60;
  private final String scheme;
  private final String host;
  private final String version;
  private final Map<String, String> headers;
  private final int connectionTimeout;
  private final int connectionRequestTimeout;
  private final int socketTimeout;

  public Config(String scheme, String host) {
    this(scheme, host, null, DEFAULT_TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS);
  }

  public Config(String scheme, String host, Map<String, String> headers) {
    this(scheme, host, headers, DEFAULT_TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS);
  }

  public Config(String scheme, String host, Map<String, String> headers, int connectionTimeout, int connectionRequestTimeout, int socketTimeout) {
    this.scheme = scheme;
    this.host = host;
    this.version = "v1";
    this.headers = headers;
    this.connectionTimeout = connectionTimeout;
    this.connectionRequestTimeout = connectionRequestTimeout;
    this.socketTimeout = socketTimeout;
  }

  public String getBaseURL() {
    return scheme + "://" + host + "/" + version;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  public int getConnectionRequestTimeout() {
    return connectionRequestTimeout;
  }

  public int getSocketTimeout() {
    return socketTimeout;
  }
}
