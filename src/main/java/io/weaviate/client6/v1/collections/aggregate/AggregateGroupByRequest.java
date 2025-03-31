package io.weaviate.client6.v1.collections.aggregate;

import java.util.function.Consumer;

public record AggregateGroupByRequest(AggregateRequest aggregate, GroupBy groupBy) {

  public static record GroupBy(String property) {
    public static GroupBy with(Consumer<Builder> options) {
      var opt = new Builder(options);
      return new GroupBy(opt.property);
    }

    public static class Builder {
      private String property;

      public Builder property(String name) {
        this.property = name;
        return this;
      }

      Builder(Consumer<Builder> options) {
        options.accept(this);
      }
    }
  }
}
