package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateMetadata;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record QueryMetadata(String uuid,
    Vectors vectors,
    Long creationTimeUnix,
    Long lastUpdateTimeUnix,
    Float distance,
    Float certainty,
    Float score,
    String explainScore) implements WeaviateMetadata {

  private QueryMetadata(Builder builder) {
    this(
        builder.uuid,
        builder.vectors,
        builder.creationTimeUnix,
        builder.lastUpdateTimeUnix,
        builder.distance,
        builder.certainty,
        builder.score,
        builder.explainScore);
  }

  public static class Builder implements ObjectBuilder<QueryMetadata> {
    private String uuid;
    private Vectors vectors;
    private Long creationTimeUnix;
    private Long lastUpdateTimeUnix;
    private Float distance;
    private Float certainty;
    private Float score;
    private String explainScore;

    public final Builder uuid(String uuid) {
      this.uuid = uuid;
      return this;
    }

    public final Builder vectors(Vectors vectors) {
      this.vectors = vectors;
      return this;
    }

    public final Builder creationTimeUnix(Long creationTimeUnix) {
      this.creationTimeUnix = creationTimeUnix;
      return this;
    }

    public final Builder lastUpdateTimeUnix(Long lastUpdateTimeUnix) {
      this.lastUpdateTimeUnix = lastUpdateTimeUnix;
      return this;
    }

    public final Builder distance(Float distance) {
      this.distance = distance;
      return this;
    }

    public final Builder certainty(Float certainty) {
      this.certainty = certainty;
      return this;
    }

    public final Builder score(Float score) {
      this.score = score;
      return this;
    }

    public final Builder explainScore(String explainScore) {
      this.explainScore = explainScore;
      return this;
    }

    @Override
    public final QueryMetadata build() {
      return new QueryMetadata(this);
    }
  }
}
