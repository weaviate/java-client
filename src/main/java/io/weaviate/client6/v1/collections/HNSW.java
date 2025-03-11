package io.weaviate.client6.v1.collections;

import java.util.function.Consumer;

public final record HNSW(Distance distance, boolean skip) implements VectorIndex.Configuration {
  public enum Distance {
    COSINE;
  }

  public static HNSW with(Consumer<Options> options) {
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
