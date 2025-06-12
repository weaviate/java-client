package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record FetchObjects(BaseQueryOptions common) implements SearchOperator {

  public static FetchObjects of(Function<Builder, ObjectBuilder<FetchObjects>> fn) {
    return fn.apply(new Builder()).build();
  }

  public FetchObjects(Builder builder) {
    this(builder.baseOptions());
  }

  public static class Builder extends BaseQueryOptions.Builder<Builder, FetchObjects> {

    @Override
    public final FetchObjects build() {
      return new FetchObjects(this);
    }
  }

  @Override
  public void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);
  }
}
