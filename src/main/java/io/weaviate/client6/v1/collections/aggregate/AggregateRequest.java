package io.weaviate.client6.v1.collections.aggregate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest.Aggregation;

public record AggregateRequest(String collectionName, Integer objectLimit, GroupBy groupBy,
    List<Metric<? extends Metric<?>>> returnMetrics, boolean includeTotalCount) {

  public static AggregateRequest with(String collectionName, Consumer<Builder> options) {
    var opt = new Builder(options);
    return new AggregateRequest(collectionName, opt.objectLimit, null, opt.metrics, opt.includeTotalCount);
  }

  public static AggregateRequest with(String collectionName, Consumer<Builder> options,
      Consumer<GroupBy.Builder> groupByOptions) {
    var opt = new Builder(options);
    return new AggregateRequest(collectionName, opt.objectLimit, GroupBy.with(groupByOptions), opt.metrics,
        opt.includeTotalCount);
  }

  void appendTo(io.weaviate.client6.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest.Builder req) {
    if (groupBy != null) {
      var by = io.weaviate.client6.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest.GroupBy.newBuilder();
      by.setCollection(collectionName);
      groupBy.appendTo(by);
      req.setGroupBy(by);
    }

    if (includeTotalCount) {
      req.setObjectsCount(true);
    }

    if (objectLimit != null) {
      req.setObjectLimit(objectLimit);
    }

    for (Metric<?> metric : returnMetrics) {
      var agg = Aggregation.newBuilder();
      metric.appendTo(agg);
      req.addAggregations(agg);
    }
  }

  public static class Builder {
    private List<Metric<? extends Metric<?>>> metrics;
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
    public final Builder metrics(Metric<? extends Metric<?>>... metrics) {
      this.metrics = Arrays.asList(metrics);
      return this;
    }
  }

  public static record GroupBy(String property) {
    public static GroupBy with(Consumer<Builder> options) {
      var opt = new Builder(options);
      return new GroupBy(opt.property);
    }

    public void appendTo(
        io.weaviate.client6.grpc.protocol.v1.WeaviateProtoAggregate.AggregateRequest.GroupBy.Builder groupBy) {
      groupBy.setProperty(property);
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
