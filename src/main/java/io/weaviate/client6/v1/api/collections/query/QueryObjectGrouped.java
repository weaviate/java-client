package io.weaviate.client6.v1.api.collections.query;

public record QueryObjectGrouped<T>(
    T properties,
    QueryObject.Metadata metadata,
    String belongsToGroup) {

  QueryObjectGrouped(QueryObject<T> object, String belongsToGroup) {
    this(object.properties(), object.metadata(), belongsToGroup);
  }
}
