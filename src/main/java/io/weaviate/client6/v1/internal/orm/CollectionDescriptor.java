package io.weaviate.client6.v1.internal.orm;

import java.util.Map;

public sealed interface CollectionDescriptor<T> permits MapDescriptor {
  String name();

  PropertiesReader<T> propertiesReader(T properties);

  PropertiesBuilder<T> propertiesBuilder();

  static CollectionDescriptor<Map<String, Object>> ofMap(String collectionName) {
    return new MapDescriptor(collectionName);
  }
}
