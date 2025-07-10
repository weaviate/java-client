package io.weaviate.client.v1.async;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.io.CloseMode;

import io.weaviate.client.Config;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.async.AsyncHttpClient;
import io.weaviate.client.base.util.DbVersionProvider;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.base.util.GrpcVersionSupport;
import io.weaviate.client.v1.async.aliases.Aliases;
import io.weaviate.client.v1.async.backup.Backup;
import io.weaviate.client.v1.async.batch.Batch;
import io.weaviate.client.v1.async.classifications.Classifications;
import io.weaviate.client.v1.async.cluster.Cluster;
import io.weaviate.client.v1.async.data.Data;
import io.weaviate.client.v1.async.graphql.GraphQL;
import io.weaviate.client.v1.async.misc.Misc;
import io.weaviate.client.v1.async.rbac.Roles;
import io.weaviate.client.v1.async.schema.Schema;
import io.weaviate.client.v1.async.users.Users;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.misc.model.Meta;

public class WeaviateAsyncClient implements AutoCloseable {
  private final Config config;
  private final CloseableHttpAsyncClient client;
  private final DbVersionSupport dbVersionSupport;
  private final GrpcVersionSupport grpcVersionSupport;
  private final AccessTokenProvider tokenProvider;

  public WeaviateAsyncClient(Config config, AccessTokenProvider tokenProvider) {
    this.config = config;
    this.client = AsyncHttpClient.create(config);
    // auto start the client
    this.start();
    // init the db version provider and get the version info
    DbVersionProvider dbVersionProvider = initDbVersionProvider();
    this.dbVersionSupport = new DbVersionSupport(dbVersionProvider);
    this.grpcVersionSupport = new GrpcVersionSupport(dbVersionProvider);
    this.tokenProvider = tokenProvider;
  }

  public Misc misc() {
    return new Misc(client, config, tokenProvider);
  }

  public Schema schema() {
    return new Schema(client, config, tokenProvider, dbVersionSupport);
  }

  public Data data() {
    return new Data(client, config, tokenProvider, dbVersionSupport);
  }

  public Batch batch() {
    return new Batch(client, config, dbVersionSupport, grpcVersionSupport, tokenProvider, data());
  }

  public Cluster cluster() {
    return new Cluster(client, config, tokenProvider);
  }

  public Classifications classifications() {
    return new Classifications(client, config, tokenProvider);
  }

  public Backup backup() {
    return new Backup(client, config, tokenProvider);
  }

  public GraphQL graphQL() {
    return new GraphQL(client, config, tokenProvider);
  }

  public Roles roles() {
    return new Roles(client, config, tokenProvider);
  }

  public Users users() {
    return new Users(client, config, tokenProvider);
  }

  public Aliases alias() {
    return new Aliases(client, config, tokenProvider);
  }

  private DbVersionProvider initDbVersionProvider() {
    DbVersionProvider.VersionGetter getter = () -> Optional.ofNullable(this.getMeta())
        .filter(result -> !result.hasErrors())
        .map(result -> result.getResult().getVersion());

    return new DbVersionProvider(getter);
  }

  private Result<Meta> getMeta() {
    try {
      return new Misc(client, config, tokenProvider).metaGetter().run().get();
    } catch (InterruptedException | ExecutionException e) {
      // we can't connect to Weaviate, metaResult will be null
      return null;
    }
  }

  private void start() {
    this.client.start();
  }

  @Override
  public void close() {
    this.client.close(CloseMode.GRACEFUL);
  }
}
