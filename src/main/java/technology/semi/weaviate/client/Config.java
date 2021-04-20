package technology.semi.weaviate.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Config {
  String scheme;
  String host;
  String version;

  public Config(String scheme, String host) {
    this.scheme = scheme;
    this.host = host;
    this.version = "v1";
  }

  public String getBaseURL() {
    return scheme + "://" + host + "/" + version;
  }
}
