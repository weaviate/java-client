package io.weaviate.client6.v1.query;

import java.util.function.Consumer;

public record NearVector(Float[] vector, Float distance, Float certainty, CommonQueryOptions common) {

  public static NearVector with(Float[] vector, Consumer<Builder> options) {
    var opt = new Builder();
    options.accept(opt);
    return new NearVector(vector, opt.distance, opt.certainty, new CommonQueryOptions(opt));
  }

  public static class Builder extends CommonQueryOptions.Builder<Builder> {
    private Float distance;
    private Float certainty;

    public Builder distance(float distance) {
      this.distance = distance;
      return this;
    }

    public Builder certainty(float certainty) {
      this.certainty = certainty;
      return this;
    }
  }
}
