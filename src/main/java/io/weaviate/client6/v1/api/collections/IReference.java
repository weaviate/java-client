package io.weaviate.client6.v1.api.collections;

import java.util.Map;

public interface IReference {
  String uuid();

  String collection();

  WeaviateObject<Map<String, Object>> asWeaviateObject();
}
