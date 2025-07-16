package io.weaviate.client6.v1.api.collections.aggregate;

import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;

public record GroupBy(String property, Integer limit) {
  public static final GroupBy property(String property) {
    return property(property, ObjectBuilder.identity());
  }

  public static final GroupBy property(String property, Function<Builder, ObjectBuilder<GroupBy>> fn) {
    return fn.apply(new Builder(property)).build();
  }

  public GroupBy(Builder builder) {
    this(builder.property, builder.limit);
  }

  public static class Builder implements ObjectBuilder<GroupBy> {
    private final String property;

    public Builder(String property) {
      this.property = property;
    }

    private Integer limit;

    public final Builder limit(int limit) {
      this.limit = limit;
      return this;
    }

    @Override
    public GroupBy build() {
      return new GroupBy(this);
    }
  }

  void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req, String collection) {
    if (limit != null) {
      req.setLimit(limit);
    }

    req.setGroupBy(WeaviateProtoAggregate.AggregateRequest.GroupBy.newBuilder()
        .setCollection(collection)
        .setProperty(property));
  }
}
