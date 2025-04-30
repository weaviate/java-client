package io.weaviate.client6.v1.collections;

import java.util.function.Consumer;

import io.weaviate.client6.v1.collections.VectorIndex.IndexType;

public final record HNSW(Distance distance, Boolean skip) implements VectorIndex.IndexingStrategy {
  public VectorIndex.IndexType type() {
    return IndexType.HNSW;
  }

  public enum Distance {
    COSINE;
  }

  HNSW() {
    this(null, null);
  }

  static HNSW with(Consumer<Builder> options) {
    var opt = new Builder(options);
    return new HNSW(opt.distance, opt.skip);
  }

  public static class Builder {
    private Distance distance;
    private Boolean skip;

    public Builder distance(Distance distance) {
      this.distance = distance;
      return this;
    }

    public Builder disableIndexation() {
      this.skip = true;
      return this;
    }

    public Builder(Consumer<Builder> options) {
      options.accept(this);
    }
  }
}
