package technology.semi.weaviate.client;

import technology.semi.weaviate.client.base.util.DbVersionProvider;
import technology.semi.weaviate.client.base.util.DbVersionSupport;
import technology.semi.weaviate.client.v1.batch.Batch;
import technology.semi.weaviate.client.v1.classifications.Classifications;
import technology.semi.weaviate.client.v1.contextionary.Contextionary;
import technology.semi.weaviate.client.v1.data.Data;
import technology.semi.weaviate.client.v1.graphql.GraphQL;
import technology.semi.weaviate.client.v1.misc.Misc;
import technology.semi.weaviate.client.v1.misc.api.MetaGetter;
import technology.semi.weaviate.client.v1.schema.Schema;

import java.util.Optional;

public class WeaviateClient {
  private final Config config;
  private final DbVersionProvider dbVersionProvider;
  private final DbVersionSupport dbVersionSupport;

  public WeaviateClient(Config config) {
    this.config = config;
    dbVersionProvider = initDbVersionProvider();
    dbVersionSupport = new DbVersionSupport(dbVersionProvider);
  }

  public Misc misc() {
    return new Misc(config, dbVersionProvider);
  }

  public Schema schema() {
    return new Schema(config);
  }

  public Data data() {
    dbVersionProvider.refresh();
    return new Data(config, dbVersionSupport);
  }

  public Batch batch() {
    dbVersionProvider.refresh();
    return new Batch(config, dbVersionSupport);
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

  private DbVersionProvider initDbVersionProvider() {
    MetaGetter metaGetter = new Misc(config, null).metaGetter();
    DbVersionProvider.VersionGetter getter = () ->
      Optional.ofNullable(metaGetter.run())
        .filter(result -> !result.hasErrors())
        .map(result -> result.getResult().getVersion());

    return new DbVersionProvider(getter);
  }
}
