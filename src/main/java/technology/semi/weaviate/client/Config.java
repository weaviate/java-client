package technology.semi.weaviate.client;

import java.util.Map;

public class Config {

  private static final int DEFAULT_TIMEOUT = 60;
  private final String scheme;
  private final String host;
  private final int connectionTimeoutMs;
  private final int connectionRequestTimeoutMs;
  private final int socketTimeoutMs;
  private final String version;
  private final Map<String, String> headers;

  public Config(String scheme, String host, Map<String, String> headers) {
    this.scheme = scheme;
    this.host = host;
    this.version = "v1";
    this.headers = headers;
    this.connectionTimeoutMs = DEFAULT_TIMEOUT;
    this.connectionRequestTimeoutMs = DEFAULT_TIMEOUT;
    this.socketTimeoutMs = DEFAULT_TIMEOUT;
  }

  public Config(String scheme, String host, Map<String, String> headers, int timeout) {
    this.scheme = scheme;
    this.host = host;
    this.version = "v1";
    this.headers = headers;
    this.connectionTimeoutMs = timeout;
    this.connectionRequestTimeoutMs = timeout;
    this.socketTimeoutMs = timeout;
  }

  public Config(String scheme, String host, Map<String, String> headers, int connectionTimeoutMs, int connectionRequestTimeoutMs, int socketTimeoutMs) {
    this.scheme = scheme;
    this.host = host;
    this.version = "v1";
    this.headers = headers;
    this.connectionTimeoutMs = connectionTimeoutMs;
    this.connectionRequestTimeoutMs = connectionRequestTimeoutMs;
    this.socketTimeoutMs = socketTimeoutMs;
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

  public int getConnectionTimeoutMs() {
    return connectionTimeoutMs;
  }

  public int getConnectionRequestTimeoutMs() {
    return connectionRequestTimeoutMs;
  }

  public int getSocketTimeoutMs() {
    return socketTimeoutMs;
  }

}
