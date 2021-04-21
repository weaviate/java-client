package technology.semi.weaviate.client.v1.misc;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.misc.api.LiveChecker;
import technology.semi.weaviate.client.v1.misc.api.MetaGetter;
import technology.semi.weaviate.client.v1.misc.api.OpenIDConfigGetter;
import technology.semi.weaviate.client.v1.misc.api.ReadyChecker;

public class Misc {
  private final Config config;

  public Misc(Config config) {
    this.config = config;
  }

  public MetaGetter metaGetter() {
    return new MetaGetter(config);
  }

  public OpenIDConfigGetter openIDConfigGetter() {
    return new OpenIDConfigGetter(config);
  }

  public LiveChecker liveChecker() {
    return new LiveChecker(config);
  }

  public ReadyChecker readyChecker() {
    return new ReadyChecker(config);
  }
}
