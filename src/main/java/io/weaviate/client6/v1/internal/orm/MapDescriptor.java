package io.weaviate.client6.v1.internal.orm;

import java.util.Map;

public class MapDescriptor implements CollectionDescriptor<Map<String, Object>> {
  private final String collectionName;

  public MapDescriptor(String collectionName) {
    this.collectionName = collectionName;
  }

  @Override
  public String name() {
    return collectionName;
  }

  @Override
  public PropertiesReader<Map<String, Object>> propertiesReader(Map<String, Object> properties) {
    return new MapReader(properties);
  }

  @Override
  public PropertiesBuilder<Map<String, Object>> propertiesBuilder() {
    return new MapBuilder();
  }
}
