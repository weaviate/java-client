package io.weaviate.client;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class Config {

  private static final int DEFAULT_TIMEOUT_SECONDS = 60;
  @Getter
  private final String scheme;
  @Getter
  private final String host;
  private final String version;
  @Getter
  private final Map<String, String> headers = new HashMap<>();
  @Getter
  private final int connectionTimeout;
  @Getter
  private final int connectionRequestTimeout;
  @Getter
  private final int socketTimeout;
  @Getter
  private String proxyHost;
  @Getter
  private int proxyPort;
  @Getter
  private String proxyScheme;
  @Getter
  @Setter
  private boolean gRPCSecured;
  @Getter
  @Setter
  private String gRPCHost;

  public Config(String scheme, String host) {
    this(scheme, host, null, DEFAULT_TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS);
  }

  public Config(String scheme, String host, boolean gRPCSecured, String gRPCHost) {
    this(scheme, host, null, DEFAULT_TIMEOUT_SECONDS, gRPCSecured, gRPCHost);
  }

  public Config(String scheme, String host, Map<String, String> headers) {
    this(scheme, host, headers, DEFAULT_TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS);
  }

  public Config(String scheme, String host, boolean gRPCSecured, String gRPCHost, Map<String, String> headers) {
    this(scheme, host, headers, DEFAULT_TIMEOUT_SECONDS, gRPCSecured, gRPCHost);
  }

  public Config(String scheme, String host, Map<String, String> headers, int connectionTimeout,
      int connectionRequestTimeout, int socketTimeout) {
    this.scheme = scheme;
    this.host = host;
    this.version = "v1";
    this.setHeaders(headers);
    this.connectionTimeout = connectionTimeout;
    this.connectionRequestTimeout = connectionRequestTimeout;
    this.socketTimeout = socketTimeout;
  }

  public Config(String scheme, String host, Map<String, String> headers, int timeout) {
    this(scheme, host, headers, timeout, false, null);
  }

  public Config(String scheme, String host, Map<String, String> headers, int timeout, boolean gRPCSecured,
      String gRPCHost) {
    this.scheme = scheme;
    this.host = host;
    this.version = "v1";
    this.setHeaders(headers);
    this.connectionTimeout = timeout;
    this.connectionRequestTimeout = timeout;
    this.socketTimeout = timeout;
    this.gRPCSecured = gRPCSecured;
    this.gRPCHost = gRPCHost;
  }

  public String getBaseURL() {
    return scheme + "://" + host + "/" + version;
  }

  public void setProxy(String proxyHost, int proxyPort, String proxyScheme) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
    this.proxyScheme = proxyScheme;
  }

  public boolean useGRPC() {
    return this.gRPCHost != null && !this.gRPCHost.trim().isEmpty();
  }

  /**
   * setHeader adds a new header to the headers map. An older entry
   * with the same key will be overwritten.
   */
  void setHeader(String key, String value) {
    this.headers.put(key, value);
  }

  /**
   * setHeaders adds all entries to the headers map. Older entries
   * with the same key will be overwritten.
   *
   * Passing a null map is safe, as it will be ignored.
   */
  void setHeaders(Map<String, String> headers) {
    if (headers != null) {
      this.headers.putAll(headers);
    }
  }
}
