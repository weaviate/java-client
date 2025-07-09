package io.weaviate.client.v1.aliases;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.aliases.api.AliasAllGetter;
import io.weaviate.client.v1.aliases.api.AliasCreator;
import io.weaviate.client.v1.aliases.api.AliasDeleter;
import io.weaviate.client.v1.aliases.api.AliasGetter;
import io.weaviate.client.v1.aliases.api.AliasUpdater;

public class Aliases {
  private final Config config;
  private final HttpClient httpClient;

  public Aliases(HttpClient httpClient, Config config) {
    this.config = config;
    this.httpClient = httpClient;
  }

  public AliasCreator creator() {
    return new AliasCreator(httpClient, config);
  }

  public AliasGetter getter() {
    return new AliasGetter(httpClient, config);
  }

  public AliasAllGetter allGetter() {
    return new AliasAllGetter(httpClient, config);
  }

  public AliasDeleter deleter() {
    return new AliasDeleter(httpClient, config);
  }

  public Object updater() {
    return new AliasUpdater(httpClient, config);
  }
}
