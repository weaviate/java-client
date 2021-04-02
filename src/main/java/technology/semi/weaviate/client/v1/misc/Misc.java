package technology.semi.weaviate.client.v1.misc;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.misc.api.LiveChecker;
import technology.semi.weaviate.client.v1.misc.api.MetaGetter;
import technology.semi.weaviate.client.v1.misc.api.OpenIDConfigGetter;
import technology.semi.weaviate.client.v1.misc.api.ReadyChecker;

public class Misc {
  private MetaGetter metaGetter;
  private OpenIDConfigGetter openIDConfigGetter;
  private LiveChecker liveChecker;
  private ReadyChecker readyChecker;

  public Misc(Config config) {
    this.metaGetter = new MetaGetter(config);
    this.openIDConfigGetter = new OpenIDConfigGetter(config);
    this.liveChecker = new LiveChecker(config);
    this.readyChecker = new ReadyChecker(config);
  }

  public MetaGetter MetaGetter() {
    return metaGetter;
  }

  public OpenIDConfigGetter OpenIDConfigGetter() {
    return openIDConfigGetter;
  }

  public LiveChecker LiveChecker() {
    return liveChecker;
  }

  public ReadyChecker ReadyChecker() {
    return readyChecker;
  }
}
