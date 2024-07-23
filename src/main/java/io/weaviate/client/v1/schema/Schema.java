package io.weaviate.client.v1.schema;

import io.weaviate.client.Config;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.v1.schema.api.ClassCreator;
import io.weaviate.client.v1.schema.api.ClassDeleter;
import io.weaviate.client.v1.schema.api.ClassExists;
import io.weaviate.client.v1.schema.api.ClassGetter;
import io.weaviate.client.v1.schema.api.ClassUpdater;
import io.weaviate.client.v1.schema.api.PropertyCreator;
import io.weaviate.client.v1.schema.api.SchemaDeleter;
import io.weaviate.client.v1.schema.api.SchemaGetter;
import io.weaviate.client.v1.schema.api.ShardUpdater;
import io.weaviate.client.v1.schema.api.ShardsGetter;
import io.weaviate.client.v1.schema.api.ShardsUpdater;
import io.weaviate.client.v1.schema.api.TenantsCreator;
import io.weaviate.client.v1.schema.api.TenantsDeleter;
import io.weaviate.client.v1.schema.api.TenantsExists;
import io.weaviate.client.v1.schema.api.TenantsGetter;
import io.weaviate.client.v1.schema.api.TenantsUpdater;

public class Schema {
  private final Config config;
  private final HttpClient httpClient;
  private final DbVersionSupport dbVersionSupport;

  public Schema(HttpClient httpClient, Config config, DbVersionSupport dbVersionSupport) {
    this.config = config;
    this.httpClient = httpClient;
    this.dbVersionSupport = dbVersionSupport;
  }

  public SchemaGetter getter() {
    return new SchemaGetter(httpClient, config);
  }

  public ClassGetter classGetter() {
    return new ClassGetter(httpClient, config);
  }

  public ClassExists exists() {
    return new ClassExists(new ClassGetter(httpClient, config));
  }

  public ClassCreator classCreator() {
    return new ClassCreator(httpClient, config);
  }

  public ClassUpdater classUpdater() {
    return new ClassUpdater(httpClient, config);
  }

  public ClassDeleter classDeleter() {
    return new ClassDeleter(httpClient, config);
  }

  public PropertyCreator propertyCreator() {
    return new PropertyCreator(httpClient, config);
  }

  public SchemaDeleter allDeleter() {
    return new SchemaDeleter(new SchemaGetter(httpClient, config), new ClassDeleter(httpClient, config));
  }

  public ShardsGetter shardsGetter() {
    return new ShardsGetter(httpClient, config);
  }

  public ShardUpdater shardUpdater() {
    return new ShardUpdater(httpClient, config);
  }

  public ShardsUpdater shardsUpdater() {
    return new ShardsUpdater(httpClient, config);
  }

  public TenantsCreator tenantsCreator() {
    return new TenantsCreator(httpClient, config);
  }

  public TenantsUpdater tenantsUpdater() {
    return new TenantsUpdater(httpClient, config, dbVersionSupport);
  }

  public TenantsDeleter tenantsDeleter() {
    return new TenantsDeleter(httpClient, config);
  }

  public TenantsGetter tenantsGetter() {
    return new TenantsGetter(httpClient, config);
  }

  public TenantsExists tenantsExists() {
    return new TenantsExists(httpClient, config);
  }
}
