package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateMetadata;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record QueryMetadata(
    /** Object UUID. */
    String uuid,
    /** Vector embeddings associated with the object. */
    Vectors vectors,
    /** Object creation time as a Unix timestamp. */
    Long createdAt,
    /** Unix timestamp of the latest object update. */
    Long lastUpdatedAt,
    /** Distances to the search vector. */
    Float distance,
    /** Distance metric normalized to {@code 0 <= c <= 1} range. */
    Float certainty,
    /** BM25 ranking score. */
    Float score,
    /** Components of the BM25 ranking score. */
    String explainScore) implements WeaviateMetadata {

  private QueryMetadata(Builder builder) {
    this(
        builder.uuid,
        builder.vectors,
        builder.createdAt,
        builder.lastUpdatedAt,
        builder.distance,
        builder.certainty,
        builder.score,
        builder.explainScore);
  }

  static class Builder implements ObjectBuilder<QueryMetadata> {
    private String uuid;
    private Vectors vectors;
    private Long createdAt;
    private Long lastUpdatedAt;
    private Float distance;
    private Float certainty;
    private Float score;
    private String explainScore;

    final Builder uuid(String uuid) {
      this.uuid = uuid;
      return this;
    }

    public Builder vectors(Vectors... vectors) {
      if (this.vectors == null) {
        this.vectors = vectors.length == 1 ? vectors[0] : new Vectors(vectors);
      } else {
        this.vectors = this.vectors.withVectors(vectors);
      }
      return this;
    }

    final Builder createdAt(Long createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    final Builder lastUpdatedAt(Long lastUpdatedAt) {
      this.lastUpdatedAt = lastUpdatedAt;
      return this;
    }

    final Builder distance(Float distance) {
      this.distance = distance;
      return this;
    }

    final Builder certainty(Float certainty) {
      this.certainty = certainty;
      return this;
    }

    final Builder score(Float score) {
      this.score = score;
      return this;
    }

    final Builder explainScore(String explainScore) {
      this.explainScore = explainScore;
      return this;
    }

    @Override
    public final QueryMetadata build() {
      return new QueryMetadata(this);
    }
  }
}
