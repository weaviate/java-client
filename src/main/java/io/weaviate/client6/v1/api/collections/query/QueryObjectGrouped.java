package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateObject;

public record QueryObjectGrouped<PropertiesT>(
    String uuid,
    Vectors vectors,
    /** Object properties. */
    PropertiesT properties,
    /** Object metadata. */
    QueryMetadata metadata,
    /** Name of the group that the object belongs to. */
    String belongsToGroup) {

  QueryObjectGrouped(WeaviateObject<PropertiesT> object,
      String belongsToGroup) {
    this(
        object.uuid(),
        object.vectors(),
        object.properties(),
        object.queryMetadata(),
        belongsToGroup);
  }
}
