package technology.semi.weaviate.client.base.util;

import java.util.Optional;

public class DbVersionProvider {

  private static final String EMPTY_VERSION = "";

  private final VersionGetter getter;
  private String version;


  public DbVersionProvider(VersionGetter getter) {
    this.getter = getter;
    this.version = EMPTY_VERSION;
  }


  public String getVersion() {
    refresh();
    return version;
  }

  public void refresh() {
    refresh(false);
  }

  public void refresh(boolean force) {
    if (force || EMPTY_VERSION.equals(version)) {
      this.version = getter.get().orElse(EMPTY_VERSION);
    }
  }


  public interface VersionGetter {
    Optional<String> get();
  }
}
