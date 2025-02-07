package io.weaviate.client.v1.experimental;

import io.weaviate.client.Config;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Collections {
  private final Config config;
  private final AccessTokenProvider tokenProvider;

  public Collection use(String collection) {
    return new Collection(config, tokenProvider, collection);
  }
}
