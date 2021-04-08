package technology.semi.weaviate.client;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import technology.semi.weaviate.client.v1.batch.Batch;
import technology.semi.weaviate.client.v1.contextionary.Contextionary;
import technology.semi.weaviate.client.v1.data.Data;
import technology.semi.weaviate.client.v1.misc.Misc;
import technology.semi.weaviate.client.v1.schema.Schema;

public class WeaviateClient {
  private Config config;
  private URLConnection connection;
  private URL url;

  public WeaviateClient(Config config) throws IOException {
    this.config = config;
    url = new URL(config.getScheme() + "://" + config.getHost());
    connection = url.openConnection();
  }

  public Misc misc() {
    return new Misc(config);
  }

  public Schema schema() {
    return new Schema(config);
  }

  public Data data() {
    return new Data(config);
  }

  public Batch batch() {
    return new Batch(config);
  }

  public Contextionary c11y() {
    return new Contextionary(config);
  }
}
