package io.weaviate.client6.v1;

import java.util.function.Consumer;

public record ObjectMetadata(String id, Vectors vectors) {

  public static ObjectMetadata with(Consumer<Builder> options) {
    var opt = new Builder(options);
    return new ObjectMetadata(opt.id, opt.vectors);
  }

  public static class Builder {
    public String id;
    public Vectors vectors;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder vectors(Vectors vectors) {
      this.vectors = vectors;
      return this;
    }

    public Builder vectors(Float[] vector) {
      this.vectors = Vectors.of(vector);
      return this;
    }

    public Builder vectors(Float[][] vector) {
      this.vectors = Vectors.of(vector);
      return this;
    }

    public Builder vectors(String name, Float[] vector) {
      this.vectors = Vectors.of(name, vector);
      return this;
    }

    public Builder vectors(String name, Float[][] vector) {
      this.vectors = Vectors.of(name, vector);
      return this;
    }

    public Builder vectors(Consumer<Vectors.NamedVectors> named) {
      this.vectors = Vectors.with(named);
      return this;
    }

    private Builder(Consumer<Builder> options) {
      options.accept(this);
    }
  }
}
