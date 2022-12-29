package technology.semi.weaviate.client;

import java.util.Map;

public class Config {

  private static final int DEFAULT_TIMEOUT = 60;
  private final String scheme;
  private final String host;
  private final int connectionTimeout;
  private final int connectionRequestTimeout;
  private final int socketTimeout;
  private final String version;
  private final Map<String, String> headers;

  public Config(String scheme, String host) {
    this(scheme, host, null);
  }

  public Config(String scheme, String host, Map<String, String> headers) {
    this(scheme, host, headers, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT);
  }

  public Config(String scheme, String host, Map<String, String> headers, int timeout) {
    this(scheme, host, headers, timeout, timeout, timeout);
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
    return this.headers;
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
