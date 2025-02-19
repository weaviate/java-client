package io.weaviate.client.v1.experimental;

import io.weaviate.client.Config;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;

public class Collection<T> {
  public final SearchClient<T> query;

  Collection(Config config, AccessTokenProvider tokenProvider, String collection, Class<T> cls) {
    this.query = new SearchClient<T>(config, tokenProvider, collection, cls);
  }
}
