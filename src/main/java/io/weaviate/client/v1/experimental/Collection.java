package io.weaviate.client.v1.experimental;

import io.weaviate.client.Config;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;

public class Collection {
  public final SearchClient query;

  Collection(Config config, AccessTokenProvider tokenProvider, String collection) {
    this.query = new SearchClient(config, tokenProvider, collection);
  }
}
