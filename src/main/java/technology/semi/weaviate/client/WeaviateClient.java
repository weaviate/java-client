package technology.semi.weaviate.client;

import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.batch.Batch;
import technology.semi.weaviate.client.v1.classifications.Classifications;
import technology.semi.weaviate.client.v1.contextionary.Contextionary;
import technology.semi.weaviate.client.v1.data.Data;
import technology.semi.weaviate.client.v1.graphql.GraphQL;
import technology.semi.weaviate.client.v1.misc.Misc;
import technology.semi.weaviate.client.v1.misc.model.Meta;
import technology.semi.weaviate.client.v1.schema.Schema;

public class WeaviateClient {
  private final Config config;
  private final String version;

  public WeaviateClient(Config config) {
    this.config = config;
    this.version = getWeaviateVersion();
  }

  public Misc misc() {
    return new Misc(config);
  }

  public Schema schema() {
    return new Schema(config);
  }

  public Data data() {
    return new Data(config, version);
  }

  public Batch batch() {
    return new Batch(config);
  }

  public Contextionary c11y() {
    return new Contextionary(config);
  }

  public Classifications classifications() {
    return new Classifications(config);
  }

  public GraphQL graphQL() {
    return new GraphQL(config);
  }

  private String getWeaviateVersion() {
    Result<Meta> result = new Misc(config).metaGetter().run();
    if (!result.hasErrors()) {
      return result.getResult().getVersion();
    }
    return "";
  }
}
