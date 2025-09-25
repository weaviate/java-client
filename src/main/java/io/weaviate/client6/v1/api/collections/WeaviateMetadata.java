package io.weaviate.client6.v1.api.collections;

public interface WeaviateMetadata {
  /** Object's UUID. */
  String uuid();

  /** Object's associated vector embeddings. */
  Vectors vectors();
}
