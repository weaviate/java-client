package io.weaviate.client;

import java.util.Optional;

import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.http.builder.HttpApacheClientBuilder;
import io.weaviate.client.base.http.impl.CommonsHttpClientImpl;
import io.weaviate.client.base.util.DbVersionProvider;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.base.util.GrpcVersionSupport;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.backup.Backup;
import io.weaviate.client.v1.batch.Batch;
import io.weaviate.client.v1.classifications.Classifications;
import io.weaviate.client.v1.cluster.Cluster;
import io.weaviate.client.v1.contextionary.Contextionary;
import io.weaviate.client.v1.data.Data;
import io.weaviate.client.v1.graphql.GraphQL;
import io.weaviate.client.v1.grpc.GRPC;
import io.weaviate.client.v1.misc.Misc;
import io.weaviate.client.v1.misc.api.MetaGetter;
import io.weaviate.client.v1.rbac.Roles;
import io.weaviate.client.v1.schema.Schema;
import io.weaviate.client.v1.users.Users;

public class WeaviateClient {
  private final Config config;
  private final DbVersionProvider dbVersionProvider;
  private final DbVersionSupport dbVersionSupport;
  private final GrpcVersionSupport grpcVersionSupport;
  private final HttpClient httpClient;
  private final AccessTokenProvider tokenProvider;

  public final io.weaviate.client.v1.experimental.Collections collections;
  public final io.weaviate.client.v1.experimental.DataClient datax;

  public WeaviateClient(Config config) {
    this(config, new CommonsHttpClientImpl(config.getHeaders(), null, HttpApacheClientBuilder.build(config)), null);
  }

  public WeaviateClient(Config config, AccessTokenProvider tokenProvider) {
    this(config, new CommonsHttpClientImpl(config.getHeaders(), tokenProvider, HttpApacheClientBuilder.build(config)),
        tokenProvider);
  }

  public WeaviateClient(Config config, HttpClient httpClient, AccessTokenProvider tokenProvider) {
    this.config = config;
    this.httpClient = httpClient;
    dbVersionProvider = initDbVersionProvider();
    dbVersionSupport = new DbVersionSupport(dbVersionProvider);
    grpcVersionSupport = new GrpcVersionSupport(dbVersionProvider);
    this.tokenProvider = tokenProvider;

    this.collections = new io.weaviate.client.v1.experimental.Collections(config, tokenProvider);
    this.datax = new io.weaviate.client.v1.experimental.DataClient(config, httpClient, tokenProvider, dbVersionSupport,
        grpcVersionSupport, new Data(httpClient, config, dbVersionSupport));
  }

  public WeaviateAsyncClient async() {
    return new WeaviateAsyncClient(config, tokenProvider);
  }

  public Misc misc() {
    return new Misc(httpClient, config, dbVersionProvider);
  }

  public Schema schema() {
    return new Schema(httpClient, config, dbVersionSupport);
  }

  public Data data() {
    dbVersionProvider.refresh();
    return new Data(httpClient, config, dbVersionSupport);
  }

  public Batch batch() {
    dbVersionProvider.refresh();
    return new Batch(httpClient, config, dbVersionSupport, grpcVersionSupport, tokenProvider, data());
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

  public GRPC gRPC() {
    return new GRPC(httpClient, config, tokenProvider);
  }

  public Roles roles() {
    return new Roles(httpClient, config);
  }

  public Users users() {
    return new Users(httpClient, config);
  }

  private DbVersionProvider initDbVersionProvider() {
    MetaGetter metaGetter = new Misc(httpClient, config, null).metaGetter();
    DbVersionProvider.VersionGetter getter = () -> Optional.ofNullable(metaGetter.run())
        .filter(result -> !result.hasErrors())
        .map(result -> result.getResult().getVersion());

    return new DbVersionProvider(getter);
  }
}
