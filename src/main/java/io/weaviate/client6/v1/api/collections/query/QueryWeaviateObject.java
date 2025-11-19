package io.weaviate.client6.v1.api.collections.query;

import java.util.List;
import java.util.Map;

import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.Vectors;

public record QueryWeaviateObject<PropertiesT>(
    String collection,
    PropertiesT properties,
    Map<String, List<QueryWeaviateObject<Object>>> references,
    QueryMetadata metadata) implements WeaviateObject {

  /** Shorthand for accesing objects's UUID from metadata. */
  @Override
  public String uuid() {
    return metadata.uuid();
  }

  /** Shorthand for accesing objects's vectors from metadata. */
  public Vectors vectors() {
    return metadata.vectors();
  }
}
