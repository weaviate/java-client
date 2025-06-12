package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.api.collections.WeaviateObject;

public record QueryObjectGrouped<T>(
    T properties,
    QueryMetadata metadata,
    String belongsToGroup) {

  QueryObjectGrouped(WeaviateObject<T, Object, QueryMetadata> object,
      String belongsToGroup) {
    this(object.properties(), object.metadata(), belongsToGroup);
  }
}
