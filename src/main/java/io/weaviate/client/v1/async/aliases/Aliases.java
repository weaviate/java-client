package io.weaviate.client.v1.async.aliases;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.aliases.api.AliasAllGetter;
import io.weaviate.client.v1.async.aliases.api.AliasCreator;
import io.weaviate.client.v1.async.aliases.api.AliasDeleter;
import io.weaviate.client.v1.async.aliases.api.AliasGetter;
import io.weaviate.client.v1.async.aliases.api.AliasUpdater;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Aliases {
  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final AccessTokenProvider tokenProvider;

  public AliasCreator creator() {
    return new AliasCreator(client, config, tokenProvider);
  }

  public AliasGetter getter() {
    return new AliasGetter(client, config, tokenProvider);
  }

  public AliasAllGetter allGetter() {
    return new AliasAllGetter(client, config, tokenProvider);
  }

  public AliasDeleter deleter() {
    return new AliasDeleter(client, config, tokenProvider);
  }

  public AliasUpdater updater() {
    return new AliasUpdater(client, config, tokenProvider);
  }
}
