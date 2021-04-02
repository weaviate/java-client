package technology.semi.weaviate.client;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import technology.semi.weaviate.client.v1.misc.Misc;

public class WeaviateClient {
  private Config config;
  private URLConnection connection;
  private URL url;

  private Misc misc;

  public WeaviateClient(Config config) throws IOException {
    this.config = config;
    url = new URL(config.getScheme() + "://" + config.getHost());
    connection = url.openConnection();
    this.misc = new Misc(config);
  }

  public Misc Misc() {
    return misc;
  }
}
