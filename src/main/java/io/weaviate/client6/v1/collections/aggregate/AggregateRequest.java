package io.weaviate.client6.v1.collections.aggregate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public record AggregateRequest(
    String collectionName,
    Integer objectLimit,
    boolean includeTotalCount,
    List<Metric> returnMetrics) {

  public static AggregateRequest with(String collectionName, Consumer<Builder> options) {
    var opt = new Builder(options);
    return new AggregateRequest(
        collectionName,
        opt.objectLimit,
        opt.includeTotalCount,
        opt.metrics);
  }

  public static class Builder {
    private List<Metric> metrics;
    private Integer objectLimit;
    private boolean includeTotalCount = false;

    Builder(Consumer<Builder> options) {
      options.accept(this);
    }

    public Builder objectLimit(int limit) {
      this.objectLimit = limit;
      return this;
    }

    public Builder includeTotalCount() {
      this.includeTotalCount = true;
      return this;
    }

    @SafeVarargs
    public final Builder metrics(Metric... metrics) {
      this.metrics = Arrays.asList(metrics);
      return this;
    }
  }
}
