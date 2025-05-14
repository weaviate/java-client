package io.weaviate.client6.v1.api.collections;

import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;

public record ObjectMetadata(String id, Vectors vectors) {
  public ObjectMetadata(Builder builder) {
    this(builder.id, builder.vectors);
  }

  public static ObjectMetadata of(Function<Builder, ObjectBuilder<ObjectMetadata>> fn) {
    return fn.apply(new Builder()).build();
  }

  public static class Builder implements ObjectBuilder<ObjectMetadata> {
    private String id;
    private Vectors vectors;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder vectors(Vectors vectors) {
      this.vectors = vectors;
      return this;
    }

    @Override
    public ObjectMetadata build() {
      return new ObjectMetadata(this);
    }
  }
}
