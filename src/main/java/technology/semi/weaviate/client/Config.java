package technology.semi.weaviate.client;

import java.util.HashMap;
import java.util.Map;
import technology.semi.weaviate.client.v1.auth.provider.AccessTokenProvider;
import technology.semi.weaviate.client.v1.auth.provider.AuthTokenProvider;

public class Config {
  private final String scheme;
  private final String host;
  private final String version;
  private final Map<String, String> headers;
  private final AccessTokenProvider tokenProvider;

  public Config(String scheme, String host) {
    this(scheme, host, null);
  }

  public Config(String scheme, String host, Map<String, String> headers) {
    this(scheme, host, headers, null);
  }

  public Config(String scheme, String host, Map<String, String> headers, AccessTokenProvider tokenProvider) {
    this.scheme = scheme;
    this.host = host;
    this.version = "v1";
    this.headers = headers;
    this.tokenProvider = tokenProvider;
  }

  public String getBaseURL() {
    return scheme + "://" + host + "/" + version;
  }

  public Map<String, String> getHeaders() {
    Map<String, String> allHeaders = new HashMap<>();
    if (headers != null) {
      allHeaders.putAll(headers);
    }
    if (tokenProvider != null) {
      allHeaders.put("Authorization", String.format("Bearer %s", tokenProvider.getAccessToken()));
    }
    return allHeaders;
  }
}
