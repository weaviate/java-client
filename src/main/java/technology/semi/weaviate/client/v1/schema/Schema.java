package technology.semi.weaviate.client.v1.schema;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.v1.schema.api.ClassCreator;
import technology.semi.weaviate.client.v1.schema.api.ClassDeleter;
import technology.semi.weaviate.client.v1.schema.api.PropertyCreator;
import technology.semi.weaviate.client.v1.schema.api.SchemaDeleter;
import technology.semi.weaviate.client.v1.schema.api.SchemaGetter;

public class Schema {
  private SchemaGetter schemaGetter;
  private ClassCreator classCreator;
  private ClassDeleter classDeleter;
  private PropertyCreator propertyCreator;
  private SchemaDeleter schemaDeleter;

  public Schema(Config config) {
    this.schemaGetter = new SchemaGetter(config);
    this.classCreator = new ClassCreator(config);
    this.classDeleter = new ClassDeleter(config);
    this.propertyCreator = new PropertyCreator(config);
    this.schemaDeleter = new SchemaDeleter(schemaGetter, classDeleter);
  }

  public SchemaGetter getter() {
    return schemaGetter;
  }

  public ClassCreator classCreator() {
    return classCreator;
  }

  public ClassDeleter classDeleter() {
    return classDeleter;
  }

  public PropertyCreator propertyCreator() {
    return propertyCreator;
  }

  public SchemaDeleter allDeleter() {
    return schemaDeleter;
  }
}
