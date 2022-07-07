package technology.semi.weaviate.client.base.util;

import java.util.Optional;

public class DbVersionProvider {

  private final VersionGetter getter;
  private String version;


  public DbVersionProvider(VersionGetter getter) {
    this.getter = getter;
    this.version = "";
  }


  public String getVersion() {
    return version;
  }

  public void refresh() {
    refresh(false);
  }

  public void refresh(boolean force) {
    if (force || "".equals(version)) {
      getter.get().ifPresent(version -> this.version = version);
    }
  }


  public interface VersionGetter {
    Optional<String> get();
  }
}
