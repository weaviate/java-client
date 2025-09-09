package io.weaviate.client6.v1.api.collections.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record FetchObjects(BaseQueryOptions common, List<SortBy> sortBy) implements QueryOperator {

  public static FetchObjects of(Function<Builder, ObjectBuilder<FetchObjects>> fn) {
    return fn.apply(new Builder()).build();
  }

  public FetchObjects(Builder builder) {
    this(builder.baseOptions(), builder.sortBy);
  }

  public static class Builder extends BaseQueryOptions.Builder<Builder, FetchObjects> {
    private final List<SortBy> sortBy = new ArrayList<>();

    public Builder sort(SortBy... sortBy) {
      return sort(Arrays.asList(sortBy));
    }

    public Builder sort(List<SortBy> sortBy) {
      this.sortBy.addAll(sortBy);
      return this;
    }

    @Override
    public final FetchObjects build() {
      return new FetchObjects(this);
    }
  }

  @Override
  public void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);

    for (final var sort : sortBy) {
      req.addSortBy(WeaviateProtoSearchGet.SortBy.newBuilder()
          .addAllPath(sort.path())
          .setAscending(sort.ascending()));
    }
  }
}
