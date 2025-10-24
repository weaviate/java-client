package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.api.collections.WeaviateObject;

public record QueryObjectGrouped<PropertiesT>(
    /** Object properties. */
    PropertiesT properties,
    /** Object metadata. */
    QueryMetadata metadata,
    /** Name of the group that the object belongs to. */
    String belongsToGroup) {

  QueryObjectGrouped(WeaviateObject<PropertiesT, Object, QueryMetadata> object,
      String belongsToGroup) {
    this(object.properties(), object.metadata(), belongsToGroup);
  }
}
