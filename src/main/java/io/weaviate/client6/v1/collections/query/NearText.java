package io.weaviate.client6.v1.collections.query;

import java.util.List;
import java.util.function.Consumer;

public record NearText(List<String> text, Float distance, Float certainty, CommonQueryOptions common) {

  public static NearText with(String text, Consumer<Builder> fn) {
    return with(List.of(text), fn);
  }

  public static NearText with(List<String> text, Consumer<Builder> fn) {
    var opt = new Builder();
    fn.accept(opt);
    return new NearText(text, opt.distance, opt.certainty, new CommonQueryOptions(opt));
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
