package io.weaviate.client6.v1.collections.query;

import java.util.function.Consumer;

public record NearImage(String image, Float distance, Float certainty, CommonQueryOptions common) {

  public static NearImage with(String image, Consumer<Builder> fn) {
    var opt = new Builder();
    fn.accept(opt);
    return new NearImage(image, opt.distance, opt.certainty, new CommonQueryOptions(opt));
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

  public static record GroupBy(String property, int maxGroups, int maxObjectsPerGroup) {
  }
}
