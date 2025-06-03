package io.weaviate.client.v1.async.schema;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

import io.weaviate.client.Config;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.v1.async.schema.api.ClassCreator;
import io.weaviate.client.v1.async.schema.api.ClassDeleter;
import io.weaviate.client.v1.async.schema.api.ClassExists;
import io.weaviate.client.v1.async.schema.api.ClassGetter;
import io.weaviate.client.v1.async.schema.api.ClassUpdater;
import io.weaviate.client.v1.async.schema.api.PropertyCreator;
import io.weaviate.client.v1.async.schema.api.SchemaDeleter;
import io.weaviate.client.v1.async.schema.api.SchemaGetter;
import io.weaviate.client.v1.async.schema.api.ShardUpdater;
import io.weaviate.client.v1.async.schema.api.ShardsGetter;
import io.weaviate.client.v1.async.schema.api.ShardsUpdater;
import io.weaviate.client.v1.async.schema.api.TenantsCreator;
import io.weaviate.client.v1.async.schema.api.TenantsDeleter;
import io.weaviate.client.v1.async.schema.api.TenantsExists;
import io.weaviate.client.v1.async.schema.api.TenantsGetter;
import io.weaviate.client.v1.async.schema.api.TenantsUpdater;
import io.weaviate.client.v1.async.schema.api.VectorAdder;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;

public class Schema {
  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final AccessTokenProvider tokenProvider;
  private final DbVersionSupport dbVersionSupport;

  public Schema(CloseableHttpAsyncClient client, Config config, AccessTokenProvider tokenProvider,
      DbVersionSupport dbVersionSupport) {
    this.client = client;
    this.config = config;
    this.tokenProvider = tokenProvider;
    this.dbVersionSupport = dbVersionSupport;
  }

  public SchemaGetter getter() {
    return new SchemaGetter(client, config, tokenProvider);
  }

  public ClassGetter classGetter() {
    return new ClassGetter(client, config, tokenProvider);
  }

  public ClassExists exists() {
    return new ClassExists(client, config, tokenProvider);
  }

  public ClassCreator classCreator() {
    return new ClassCreator(client, config, tokenProvider);
  }

  public ClassUpdater classUpdater() {
    return new ClassUpdater(client, config, tokenProvider);
  }

  public ClassDeleter classDeleter() {
    return new ClassDeleter(client, config, tokenProvider);
  }

  public PropertyCreator propertyCreator() {
    return new PropertyCreator(client, config, tokenProvider);
  }

  public VectorAdder vectorAdder() {
    return new VectorAdder(client, config, tokenProvider);
  }

  public SchemaDeleter allDeleter() {
    return new SchemaDeleter(new SchemaGetter(client, config, tokenProvider),
        new ClassDeleter(client, config, tokenProvider));
  }

  public ShardsGetter shardsGetter() {
    return new ShardsGetter(client, config, tokenProvider);
  }

  public ShardUpdater shardUpdater() {
    return new ShardUpdater(client, config, tokenProvider);
  }

  public ShardsUpdater shardsUpdater() {
    return new ShardsUpdater(client, config, tokenProvider);
  }

  public TenantsCreator tenantsCreator() {
    return new TenantsCreator(client, config, tokenProvider);
  }

  public TenantsUpdater tenantsUpdater() {
    return new TenantsUpdater(client, config, tokenProvider, dbVersionSupport);
  }

  public TenantsExists tenantsExists() {
    return new TenantsExists(client, config, tokenProvider);
  }

  public TenantsGetter tenantsGetter() {
    return new TenantsGetter(client, config, tokenProvider);
  }

  public TenantsDeleter tenantsDeleter() {
    return new TenantsDeleter(client, config, tokenProvider);
  }
}
