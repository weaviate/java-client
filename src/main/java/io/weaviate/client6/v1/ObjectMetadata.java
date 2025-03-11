package io.weaviate.client6.v1;

import java.util.function.Consumer;

public class ObjectMetadata {
  public final String id;
  public final Vectors vectors;

  // ObjectMetadata(String id, Vectors vectors) {
  // this(m -> m.id(id).vectors(vectors));
  // }

  public ObjectMetadata(String id, Vectors vectors) {
    this.id = id;
    this.vectors = vectors;
  }

  public ObjectMetadata(Consumer<Builder> options) {
    var opt = new Builder(options);

    this.id = opt.id;
    this.vectors = opt.vectors;
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
      this.vectors = new Vectors(named);
      return this;
    }

    private Builder(Consumer<Builder> options) {
      options.accept(this);
    }
  }
}
