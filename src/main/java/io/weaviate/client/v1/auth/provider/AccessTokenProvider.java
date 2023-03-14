package io.weaviate.client.v1.auth.provider;

public interface AccessTokenProvider {
  String getAccessToken();
  void shutdown();
}
