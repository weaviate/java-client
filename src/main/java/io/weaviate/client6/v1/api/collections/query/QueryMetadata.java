package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record QueryMetadata(
    Float distance,
    /** Distance metric normalized to {@code 0 <= c <= 1} range. */
    Float certainty,
    /** BM25 ranking score. */
    Float score,
    /** Components of the BM25 ranking score. */
    String explainScore) {

  private QueryMetadata(Builder builder) {
    this(
        builder.distance,
        builder.certainty,
        builder.score,
        builder.explainScore);
  }

  static class Builder implements ObjectBuilder<QueryMetadata> {
    private Float distance;
    private Float certainty;
    private Float score;
    private String explainScore;

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
