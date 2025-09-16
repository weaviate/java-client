package io.weaviate.client6.v1.internal.orm;

import java.util.Map;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public sealed interface CollectionDescriptor<PropertiesT> permits MapDescriptor, PojoDescriptor {
  String name();

  TypeToken<PropertiesT> typeToken();

  PropertiesReader<PropertiesT> propertiesReader(PropertiesT properties);

  PropertiesBuilder<PropertiesT> propertiesBuilder();

  default Function<CollectionConfig.Builder, ObjectBuilder<CollectionConfig>> configFn() {
    return ObjectBuilder.identity();
  }

  // default Function<CollectionConfig.Builder, ObjectBuilder<CollectionConfig>>
  // partial(
  // Function<CollectionConfig.Builder, ObjectBuilder<CollectionConfig>> fn) {
  // return configFn().andThen(b -> fn.apply((CollectionConfig.Builder) b));
  // }

  static CollectionDescriptor<Map<String, Object>> ofMap(String collectionName) {
    return new MapDescriptor(collectionName);
  }

  static <PropertiesT extends Record> CollectionDescriptor<PropertiesT> ofClass(Class<PropertiesT> cls) {
    return new PojoDescriptor<>(cls);
  }
}
