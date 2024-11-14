package io.weaviate.client.v1.async;

import io.weaviate.client.Config;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.async.AsyncHttpClient;
import io.weaviate.client.base.util.DbVersionProvider;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.v1.async.batch.Batch;
import io.weaviate.client.v1.async.classifications.Classifications;
import io.weaviate.client.v1.async.cluster.Cluster;
import io.weaviate.client.v1.async.data.Data;
import io.weaviate.client.v1.async.misc.Misc;
import io.weaviate.client.v1.async.schema.Schema;
import io.weaviate.client.v1.misc.model.Meta;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.io.CloseMode;

public class WeaviateAsyncClient implements AutoCloseable {
  private final Config config;
  private final CloseableHttpAsyncClient client;
  private final DbVersionSupport dbVersionSupport;

  public WeaviateAsyncClient(Config config) {
    this.config = config;
    this.client = AsyncHttpClient.create(config);
    // auto start the client
    this.start();
    // init the db version provider and get the version info
    this.dbVersionSupport = new DbVersionSupport(initDbVersionProvider());
  }

  public Misc misc() {
    return new Misc(client, config);
  }

  public Schema schema() {
    return new Schema(client, config);
  }

  public Data data() {
    return new Data(client, config, dbVersionSupport);
  }

  public Batch batch() {
    return new Batch(client, config, dbVersionSupport, data());
  }

  public Cluster cluster() {
    return new Cluster(client, config);
  }

  public Classifications classifications() {
    return new Classifications(client, config);
  }

  private DbVersionProvider initDbVersionProvider() {
    DbVersionProvider.VersionGetter getter = () ->
      Optional.ofNullable(this.getMeta())
        .filter(result -> !result.hasErrors())
        .map(result -> result.getResult().getVersion());

    return new DbVersionProvider(getter);
  }

  private Result<Meta> getMeta() {
    try {
      return new Misc(client, config).metaGetter().run().get();
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
