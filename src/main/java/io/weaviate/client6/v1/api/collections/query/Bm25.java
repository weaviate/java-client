package io.weaviate.client6.v1.api.collections.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record Bm25(
    String query,
    List<String> queryProperties,
    SearchOperator searchOperator,
    BaseQueryOptions common) implements QueryOperator {

  public static final Bm25 of(String query) {
    return of(query, ObjectBuilder.identity());
  }

  public static final Bm25 of(String query, Function<Builder, ObjectBuilder<Bm25>> fn) {
    return fn.apply(new Builder(query)).build();
  }

  public Bm25(Builder builder) {
    this(builder.query, builder.queryProperties, builder.searchOperator, builder.baseOptions());
  }

  public static class Builder extends BaseQueryOptions.Builder<Builder, Bm25> {
    // Required query parameters.
    private final String query;

    // Optional query parameters.
    List<String> queryProperties = new ArrayList<>();
    SearchOperator searchOperator;

    public Builder(String query) {
      this.query = query;
    }

    /** Select properties to be included in the results scoring. */
    public Builder queryProperties(String... properties) {
      return queryProperties(Arrays.asList(properties));
    }

    /** Select properties to be included in the results scoring. */
    public Builder queryProperties(List<String> properties) {
      this.queryProperties.addAll(properties);
      return this;
    }

    /**
     * Select <a href=
     * "https://docs.weaviate.io/weaviate/api/graphql/search-operators#bm25">BM25
     * Search Operator</a> to use.
     */
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
    var bm25 = WeaviateProtoBaseSearch.BM25.newBuilder()
        .setQuery(query)
        .addAllProperties(queryProperties);

    if (searchOperator != null) {
      searchOperator.appendTo(bm25);
    }
    req.setBm25Search(bm25);
  }
}
