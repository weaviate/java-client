package io.weaviate.client.v1.async.schema;

import io.weaviate.client.Config;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.v1.async.schema.api.ClassCreator;
import io.weaviate.client.v1.async.schema.api.ClassDeleter;
import io.weaviate.client.v1.async.schema.api.ClassExists;
import io.weaviate.client.v1.async.schema.api.ClassGetter;
import io.weaviate.client.v1.async.schema.api.ClassUpdater;
import io.weaviate.client.v1.async.schema.api.SchemaGetter;
import io.weaviate.client.v1.async.schema.api.PropertyCreator;
import io.weaviate.client.v1.async.schema.api.SchemaDeleter;
import io.weaviate.client.v1.async.schema.api.ShardsGetter;
import io.weaviate.client.v1.async.schema.api.ShardUpdater;
import io.weaviate.client.v1.async.schema.api.ShardsUpdater;
import io.weaviate.client.v1.async.schema.api.TenantsCreator;
import io.weaviate.client.v1.async.schema.api.TenantsGetter;
import io.weaviate.client.v1.async.schema.api.TenantsUpdater;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

public class Schema {
  private final CloseableHttpAsyncClient client;
  private final Config config;
  private final DbVersionSupport dbVersionSupport;

  public Schema(CloseableHttpAsyncClient client, Config config, DbVersionSupport dbVersionSupport) {
    this.client = client;
    this.config = config;
    this.dbVersionSupport = dbVersionSupport;
  }

  public SchemaGetter getter() {
    return new SchemaGetter(client, config);
  }

  public ClassGetter classGetter() {
    return new ClassGetter(client, config);
  }

  public ClassExists exists() {
    return new ClassExists(client, config);
  }

  public ClassCreator classCreator() {
    return new ClassCreator(client, config);
  }

  public ClassUpdater classUpdater() {
    return new ClassUpdater(client, config);
  }

  public ClassDeleter classDeleter() {
    return new ClassDeleter(client, config);
  }

  public PropertyCreator propertyCreator() {
    return new PropertyCreator(client, config);
  }

  public SchemaDeleter allDeleter() {
    return new SchemaDeleter(new SchemaGetter(client, config), new ClassDeleter(client, config));
  }

  public ShardsGetter shardsGetter() {
    return new ShardsGetter(client, config);
  }

  public ShardUpdater shardUpdater() {
    return new ShardUpdater(client, config);
  }

  public ShardsUpdater shardsUpdater() {
    return new ShardsUpdater(client, config);
  }

  public TenantsCreator tenantsCreator() { // TODO
    return new TenantsCreator(client, config);
  }

  public TenantsUpdater tenantsUpdater() {
    return new TenantsUpdater(client, config, dbVersionSupport);
  }

  public TenantsGetter tenantsGetter() {
    return new TenantsGetter(client, config);
  }
}
