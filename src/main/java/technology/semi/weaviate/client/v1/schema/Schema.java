package technology.semi.weaviate.client.v1.schema;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.v1.schema.api.ClassCreator;
import technology.semi.weaviate.client.v1.schema.api.ClassDeleter;
import technology.semi.weaviate.client.v1.schema.api.ClassGetter;
import technology.semi.weaviate.client.v1.schema.api.PropertyCreator;
import technology.semi.weaviate.client.v1.schema.api.SchemaDeleter;
import technology.semi.weaviate.client.v1.schema.api.SchemaGetter;
import technology.semi.weaviate.client.v1.schema.api.ShardUpdater;
import technology.semi.weaviate.client.v1.schema.api.ShardsGetter;
import technology.semi.weaviate.client.v1.schema.api.ShardsUpdater;

public class Schema {
  private final Config config;
  private final HttpClient httpClient;

  public Schema(HttpClient httpClient, Config config) {
    this.config = config;
    this.httpClient = httpClient;
  }

  public SchemaGetter getter() {
    return new SchemaGetter(httpClient, config);
  }

  public ClassGetter classGetter() {
    return new ClassGetter(httpClient, config);
  }

  public ClassCreator classCreator() {
    return new ClassCreator(httpClient, config);
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
}
