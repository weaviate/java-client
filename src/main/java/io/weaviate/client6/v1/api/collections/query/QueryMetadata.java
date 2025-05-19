package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record QueryMetadata(String id, Float distance, Float certainty, Vectors vectors) {

  private QueryMetadata(Builder builder) {
    this(builder.uuid, builder.distance, builder.certainty, builder.vectors);
  }

  public static class Builder implements ObjectBuilder<QueryMetadata> {
    private String uuid;
    private Float distance;
    private Float certainty;
    private Vectors vectors;

    public final Builder id(String uuid) {
      this.uuid = uuid;
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

    public final Builder vectors(Vectors vectors) {
      this.vectors = vectors;
      return this;
    }

    @Override
    public final QueryMetadata build() {
      return new QueryMetadata(this);
    }
  }
}
