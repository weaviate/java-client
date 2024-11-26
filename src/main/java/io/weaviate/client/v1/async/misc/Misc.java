package io.weaviate.client.v1.async.misc;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.misc.api.LiveChecker;
import io.weaviate.client.v1.async.misc.api.MetaGetter;
import io.weaviate.client.v1.async.misc.api.OpenIDConfigGetter;
import io.weaviate.client.v1.async.misc.api.ReadyChecker;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

public class Misc {
  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final AccessTokenProvider tokenProvider;

  public Misc(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider) {
    this.client = client;
    this.config = config;
    this.tokenProvider = tokenProvider;
  }

  public MetaGetter metaGetter() {
    return new MetaGetter(client, config, tokenProvider);
  }

  public OpenIDConfigGetter openIDConfigGetter() {
    return new OpenIDConfigGetter(client, config, tokenProvider);
  }

  public LiveChecker liveChecker() {
    return new LiveChecker(client, config, tokenProvider);
  }

  public ReadyChecker readyChecker() {
    return new ReadyChecker(client, config, tokenProvider);
  }
}
