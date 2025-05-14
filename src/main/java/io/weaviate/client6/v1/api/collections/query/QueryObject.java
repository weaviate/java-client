package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record QueryObject<T>(T properties, Metadata metadata) {

  public static record Metadata(String id, Float distance, Float certainty, Vectors vector) {

    private Metadata(Builder builder) {
      this(builder.id, builder.distance, builder.certainty, builder.vectors);
    }

    public static class Builder implements ObjectBuilder<Metadata> {
      private String id;
      private Float distance;
      private Float certainty;
      private Vectors vectors;

      public final Builder id(String id) {
        this.id = id;
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
      public final Metadata build() {
        return new Metadata(this);
      }
    }
  }

}
