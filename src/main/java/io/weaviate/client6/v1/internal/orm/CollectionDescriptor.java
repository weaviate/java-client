package io.weaviate.client6.v1.internal.orm;

import java.util.Map;

import com.google.gson.reflect.TypeToken;

public sealed interface CollectionDescriptor<T> permits MapDescriptor {
  String name();

  TypeToken<T> typeToken();

  PropertiesReader<T> propertiesReader(T properties);

  PropertiesBuilder<T> propertiesBuilder();

  static CollectionDescriptor<Map<String, Object>> ofMap(String collectionName) {
    return new MapDescriptor(collectionName);
  }
}
