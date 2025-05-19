package io.weaviate.client6.v1.internal.orm;

public interface CollectionDescriptor<T> {
  String name();

  PropertiesReader<T> propertiesReader(T properties);

  PropertiesBuilder<T> propertiesBuilder();
}
