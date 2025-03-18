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

  static HNSW with(Consumer<Options> options) {
    var opt = new Options(options);
    return new HNSW(opt.distance, opt.skip);
  }

  public static class Options {
    public Distance distance;
    public Boolean skip;

    public Options distance(Distance distance) {
      this.distance = distance;
      return this;
    }

    public Options disableIndexation() {
      this.skip = true;
      return this;
    }

    public Options(Consumer<Options> options) {
      options.accept(this);
    }
  }
}
