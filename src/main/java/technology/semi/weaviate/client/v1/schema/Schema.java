package technology.semi.weaviate.client.v1.schema;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.schema.api.ClassCreator;
import technology.semi.weaviate.client.v1.schema.api.ClassDeleter;
import technology.semi.weaviate.client.v1.schema.api.PropertyCreator;
import technology.semi.weaviate.client.v1.schema.api.SchemaDeleter;
import technology.semi.weaviate.client.v1.schema.api.SchemaGetter;

public class Schema {
  private Config config;

  public Schema(Config config) {
    this.config = config;
  }

  public SchemaGetter getter() {
    return new SchemaGetter(config);
  }

  public ClassCreator classCreator() {
    return new ClassCreator(config);
  }

  public ClassDeleter classDeleter() {
    return new ClassDeleter(config);
  }

  public PropertyCreator propertyCreator() {
    return new PropertyCreator(config);
  }

  public SchemaDeleter allDeleter() {
    return new SchemaDeleter(new SchemaGetter(config), new ClassDeleter(config));
  }
}
