package io.weaviate.client.v1.auth;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.auth.exception.AuthException;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ApiKeyFlow implements Authentication {

  private final String apiKey;

  @Override
  public WeaviateClient getAuthClient(Config config, List<String> scopes) throws AuthException {
    return getAuthClient(config);
  }

  @Override
  public WeaviateClient getAuthClient(Config config) throws AuthException {
    return new WeaviateClient(config, () -> apiKey);
  }
}
