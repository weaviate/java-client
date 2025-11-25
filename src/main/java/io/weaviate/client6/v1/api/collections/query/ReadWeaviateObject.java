package io.weaviate.client6.v1.api.collections.query;

import java.util.List;
import java.util.Map;

import io.weaviate.client6.v1.api.collections.IReference;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateObject;

public record ReadWeaviateObject<PropertiesT>(
    String collection,
    PropertiesT properties,
    Map<String, List<IReference>> references,
    QueryMetadata queryMetadata) implements WeaviateObject<PropertiesT>, IReference {

  /** Shorthand for accesing objects's UUID from metadata. */
  @Override
  public String uuid() {
    return queryMetadata.uuid();
  }

  /** Shorthand for accesing objects's vectors from metadata. */
  @Override
  public Vectors vectors() {
    return queryMetadata.vectors();
  }

  @Override
  public Long createdAt() {
    return queryMetadata.createdAt();
  }

  @Override
  public Long lastUpdatedAt() {
    return queryMetadata.lastUpdatedAt();
  }

  @Override
  public String tenant() {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public WeaviateObject<Map<String, Object>> asWeaviateObject() {
    return (WeaviateObject<Map<String, Object>>) this;
  }
}
