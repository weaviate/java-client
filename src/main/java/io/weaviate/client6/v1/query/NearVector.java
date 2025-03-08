package io.weaviate.client6.v1.query;

import java.util.function.Consumer;

public class NearVector {
  private final Float[] vector;
  private final Options options;

  public static class Options extends QueryOptions<Options> {
    private Float distance;
    private Float certainty;

    public Options distance(float distance) {
      this.distance = distance;
      return this;
    }

    public Options certainty(float certainty) {
      this.certainty = certainty;
      return this;
    }
  }

  public NearVector(Float[] vector, Consumer<Options> options) {
    this.options = new Options();
    this.vector = vector;
    options.accept(this.options);
  }
}
