package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.query.Filter;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;

public record Aggregation(
    AggregateObjectFilter filter,
    Filter whereFilter,
    Integer objectLimit,
    boolean includeTotalCount,
    List<PropertyAggregation> returnMetrics) {

  public static Aggregation of() {
    return of(AggregateObjectFilter.NONE, ObjectBuilder.identity());
  }

  public static Aggregation of(Function<Builder, ObjectBuilder<Aggregation>> fn) {
    return of(AggregateObjectFilter.NONE, fn);
  }

  public static Aggregation of(AggregateObjectFilter objectFilter, Function<Builder, ObjectBuilder<Aggregation>> fn) {
    return fn.apply(new Builder(objectFilter)).build();
  }

  public Aggregation(Builder builder) {
    this(
        builder.objectFilter,
        builder.whereFilter,
        builder.objectLimit,
        builder.includeTotalCount,
        builder.metrics);
  }

  public static class Builder implements ObjectBuilder<Aggregation> {
    private final AggregateObjectFilter objectFilter;

    public Builder(AggregateObjectFilter objectFilter) {
      this.objectFilter = objectFilter;
    }

    private Filter whereFilter;
    private List<PropertyAggregation> metrics = new ArrayList<>();
    private Integer objectLimit;
    private boolean includeTotalCount = false;

    public final Builder objectLimit(int limit) {
      this.objectLimit = limit;
      return this;
    }

    public final Builder includeTotalCount(boolean include) {
      this.includeTotalCount = include;
      return this;
    }

    /**
     * Filter result set using traditional filtering operators: {@code eq},
     * {@code gte}, {@code like}, etc.
     * Subsequent calls to {@link #filter} aggregate with an AND operator.
     */
    public final Builder filters(Filter filter) {
      this.whereFilter = this.whereFilter == null
          ? filter
          : Filter.and(this.whereFilter, filter);
      return this;
    }

    /** Combine several conditions using with an AND operator. */
    public final Builder filters(Filter... filters) {
      Arrays.stream(filters).map(this::filters);
      return this;
    }

    @SafeVarargs
    public final Builder metrics(PropertyAggregation... metrics) {
      this.metrics = Arrays.asList(metrics);
      return this;
    }

    @Override
    public Aggregation build() {
      return new Aggregation(this);
    }
  }

  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req) {
    if (filter != null) {
      filter.appendTo(req);
    }

    if (includeTotalCount) {
      req.setObjectsCount(true);
    }

    if (objectLimit != null) {
      req.setObjectLimit(objectLimit);
    }

    if (whereFilter != null) {
      var protoFilters = WeaviateProtoBase.Filters.newBuilder();
      whereFilter.appendTo(protoFilters);
      req.setFilters(protoFilters);
    }

    for (final var metric : returnMetrics) {
      var aggregation = WeaviateProtoAggregate.AggregateRequest.Aggregation.newBuilder();
      metric.appendTo(aggregation);
      req.addAggregations(aggregation);
    }
  }
}
