package io.weaviate.client6.v1.api.collections.query;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record Bm25(String query, List<String> queryProperties, BaseQueryOptions common)
    implements QueryOperator {

  public static final Bm25 of(String query) {
    return of(query, ObjectBuilder.identity());
  }

  public static final Bm25 of(String query, Function<Builder, ObjectBuilder<Bm25>> fn) {
    return fn.apply(new Builder(query)).build();
  }

  public Bm25(Builder builder) {
    this(builder.query, builder.queryProperties, builder.baseOptions());
  }

  public static class Builder extends BaseQueryOptions.Builder<Builder, Bm25> {
    // Required query parameters.
    private final String query;

    // Optional query parameters.
    List<String> queryProperties;
    SearchOperator searchOperator;

    public Builder(String query) {
      this.query = query;
    }

    public Builder queryProperties(String... properties) {
      return queryProperties(Arrays.asList(properties));
    }

    public Builder queryProperties(List<String> properties) {
      this.queryProperties = properties;
      return this;
    }

    public Builder searchOperator(SearchOperator searchOperator) {
      this.searchOperator = searchOperator;
      return this;
    }

    @Override
    public final Bm25 build() {
      return new Bm25(this);
    }
  }

  @Override
  public final void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);
    req.setBm25Search(WeaviateProtoBaseSearch.BM25.newBuilder()
        .setQuery(query)
        .addAllProperties(queryProperties));
  }
}
