package technology.semi.weaviate.client;

import java.util.Optional;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.base.http.builder.HttpApacheClientBuilder;
import technology.semi.weaviate.client.base.http.impl.CommonsHttpClientImpl;
import technology.semi.weaviate.client.base.util.DbVersionProvider;
import technology.semi.weaviate.client.base.util.DbVersionSupport;
import technology.semi.weaviate.client.v1.auth.provider.AccessTokenProvider;
import technology.semi.weaviate.client.v1.backup.Backup;
import technology.semi.weaviate.client.v1.batch.Batch;
import technology.semi.weaviate.client.v1.classifications.Classifications;
import technology.semi.weaviate.client.v1.cluster.Cluster;
import technology.semi.weaviate.client.v1.contextionary.Contextionary;
import technology.semi.weaviate.client.v1.data.Data;
import technology.semi.weaviate.client.v1.graphql.GraphQL;
import technology.semi.weaviate.client.v1.misc.Misc;
import technology.semi.weaviate.client.v1.misc.api.MetaGetter;
import technology.semi.weaviate.client.v1.schema.Schema;

public class WeaviateClient {
  private final Config config;
  private final DbVersionProvider dbVersionProvider;
  private final DbVersionSupport dbVersionSupport;
  private final HttpClient httpClient;

  public WeaviateClient(Config config) {
    this(config, new CommonsHttpClientImpl(config.getHeaders(), null, HttpApacheClientBuilder.build(config)));
  }

  public WeaviateClient(Config config, AccessTokenProvider tokenProvider) {
    this(config, new CommonsHttpClientImpl(config.getHeaders(), tokenProvider, HttpApacheClientBuilder.build(config)));
  }

  public WeaviateClient(Config config, HttpClient httpClient) {
    this.config = config;
    this.httpClient = httpClient;
    dbVersionProvider = initDbVersionProvider();
    dbVersionSupport = new DbVersionSupport(dbVersionProvider);
  }

  public Misc misc() {
    return new Misc(httpClient, config, dbVersionProvider);
  }

  public Schema schema() {
    return new Schema(httpClient, config);
  }

  public Data data() {
    dbVersionProvider.refresh();
    return new Data(httpClient, config, dbVersionSupport);
  }

  public Batch batch() {
    dbVersionProvider.refresh();
    return new Batch(httpClient, config, dbVersionSupport, data());
  }

  public Backup backup() {
    return new Backup(httpClient, config);
  }

  public Contextionary c11y() {
    return new Contextionary(httpClient, config);
  }

  public Classifications classifications() {
    return new Classifications(httpClient, config);
  }

  public Cluster cluster() {
    return new Cluster(httpClient, config);
  }
  public GraphQL graphQL() {
    return new GraphQL(httpClient, config);
  }

  private DbVersionProvider initDbVersionProvider() {
    MetaGetter metaGetter = new Misc(httpClient, config, null).metaGetter();
    DbVersionProvider.VersionGetter getter = () ->
      Optional.ofNullable(metaGetter.run())
        .filter(result -> !result.hasErrors())
        .map(result -> result.getResult().getVersion());

    return new DbVersionProvider(getter);
  }
}
