package io.weaviate.client6.v1.internal.orm;

import java.util.Map;

public interface PropertiesReader<T> {
  Map<String, Object> readProperties();
}
