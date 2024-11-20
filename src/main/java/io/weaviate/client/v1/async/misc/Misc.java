package io.weaviate.client.v1.async.misc;

import io.weaviate.client.Config;
import io.weaviate.client.v1.async.misc.api.LiveChecker;
import io.weaviate.client.v1.async.misc.api.MetaGetter;
import io.weaviate.client.v1.async.misc.api.OpenIDConfigGetter;
import io.weaviate.client.v1.async.misc.api.ReadyChecker;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

public class Misc {
  private final CloseableHttpAsyncClient client;
  private final Config config;

  public Misc(CloseableHttpAsyncClient client, Config config) {
    this.client = client;
    this.config = config;
  }

  public MetaGetter metaGetter() {
    return new MetaGetter(client, config);
  }

  public OpenIDConfigGetter openIDConfigGetter() {
    return new OpenIDConfigGetter(client, config);
  }

  public LiveChecker liveChecker() {
    return new LiveChecker(client, config);
  }

  public ReadyChecker readyChecker() {
    return new ReadyChecker(client, config);
  }
}
