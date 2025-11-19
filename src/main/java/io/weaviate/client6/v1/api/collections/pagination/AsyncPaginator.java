package io.weaviate.client6.v1.api.collections.pagination;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.query.FetchObjects;
import io.weaviate.client6.v1.api.collections.query.Metadata;
import io.weaviate.client6.v1.api.collections.query.QueryReference;
import io.weaviate.client6.v1.api.collections.query.QueryResponse;
import io.weaviate.client6.v1.api.collections.query.QueryWeaviateObject;
import io.weaviate.client6.v1.api.collections.query.WeaviateQueryClientAsync;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public class AsyncPaginator<PropertiesT> {
  private final WeaviateQueryClientAsync<PropertiesT> query;
  private final Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> queryOptions;
  private final int pageSize;
  private final String cursor;

  private CompletableFuture<AsyncPage<PropertiesT>> resultSet;

  public AsyncPaginator(Builder<PropertiesT> builder) {
    this.query = builder.query;
    this.queryOptions = builder.queryOptions;
    this.pageSize = builder.pageSize;
    this.cursor = builder.cursor;

    var rs = new AsyncPage<PropertiesT>(
        cursor,
        pageSize,
        (cursor, pageSize) -> {
          var fn = ObjectBuilder.partial(queryOptions, q -> q.after(cursor).limit(pageSize));
          return this.query.fetchObjects(fn)
              .handle((response, ex) -> {
                if (ex != null) {
                  throw PaginationException.after(cursor, pageSize, ex);
                }
                return response;
              })
              .thenApply(QueryResponse::objects);
        });

    this.resultSet = builder.prefetch ? rs.fetchNextPage() : CompletableFuture.completedFuture(rs);
  }

  public CompletableFuture<Void> forEach(Consumer<QueryWeaviateObject<PropertiesT>> action) {
    return resultSet
        .thenCompose(rs -> rs.isEmpty() ? rs.fetchNextPage() : CompletableFuture.completedFuture(rs))
        .thenCompose(processEachAndAdvance(action));
  }

  public CompletableFuture<Void> forPage(Consumer<List<QueryWeaviateObject<PropertiesT>>> action) {
    return resultSet
        .thenCompose(rs -> rs.isEmpty() ? rs.fetchNextPage() : CompletableFuture.completedFuture(rs))
        .thenCompose(processPageAndAdvance(action));
  }

  private static <PropertiesT> Function<AsyncPage<PropertiesT>, CompletableFuture<Void>> processEachAndAdvance(
      Consumer<QueryWeaviateObject<PropertiesT>> action) {
    return processAndAdvanceFunc(rs -> rs.forEach(action));
  }

  private static <PropertiesT> Function<AsyncPage<PropertiesT>, CompletableFuture<Void>> processPageAndAdvance(
      Consumer<List<QueryWeaviateObject<PropertiesT>>> action) {
    return processAndAdvanceFunc(rs -> action.accept(rs.items()));
  }

  private static <PropertiesT> Function<AsyncPage<PropertiesT>, CompletableFuture<Void>> processAndAdvanceFunc(
      Consumer<AsyncPage<PropertiesT>> action) {
    return rs -> {
      // Empty result set means there were no more objects to fetch.
      if (rs.isEmpty()) {
        return CompletableFuture.completedFuture(null);
      }

      // Apply provided callback for each method -- consume current page.
      action.accept(rs);

      // Advance iteration.
      return rs.fetchNextPage().thenCompose(processAndAdvanceFunc(action));
    };
  }

  public static <PropertiesT> AsyncPaginator<PropertiesT> of(WeaviateQueryClientAsync<PropertiesT> query) {
    return of(query, ObjectBuilder.identity());
  }

  public static <PropertiesT> AsyncPaginator<PropertiesT> of(WeaviateQueryClientAsync<PropertiesT> query,
      Function<Builder<PropertiesT>, ObjectBuilder<AsyncPaginator<PropertiesT>>> fn) {
    return fn.apply(new Builder<>(query)).build();
  }

  public static class Builder<PropertiesT> implements ObjectBuilder<AsyncPaginator<PropertiesT>> {
    private final WeaviateQueryClientAsync<PropertiesT> query;

    private Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> queryOptions = ObjectBuilder.identity();
    private int pageSize = Paginator.DEFAULT_PAGE_SIZE;
    private String cursor;
    private boolean prefetch = false;

    public Builder(WeaviateQueryClientAsync<PropertiesT> query) {
      this.query = query;
    }

    // Pagination options -----------------------------------------------------

    public Builder<PropertiesT> pageSize(int pageSize) {
      this.pageSize = pageSize;
      return this;
    }

    /** Set a cursor (object UUID) to start pagination from. */
    public Builder<PropertiesT> fromCursor(String uuid) {
      this.cursor = uuid;
      return this;
    }

    /**
     * When prefetch is enabled, the first page is retrieved before any of the
     * terminating methods ({@link AsyncPaginator#forEach},
     * {@link AsyncPaginator#forPage}) are called on the paginator.
     */
    public Builder<PropertiesT> prefetch(boolean enable) {
      this.prefetch = enable;
      return this;
    }

    // Query options ----------------------------------------------------------

    public final Builder<PropertiesT> returnProperties(String... properties) {
      return applyQueryOption(q -> q.returnProperties(properties));
    }

    public final Builder<PropertiesT> returnProperties(List<String> properties) {
      return applyQueryOption(q -> q.returnProperties(properties));
    }

    public final Builder<PropertiesT> returnReferences(QueryReference... references) {
      return applyQueryOption(q -> q.returnReferences(references));
    }

    public final Builder<PropertiesT> returnReferences(List<QueryReference> references) {
      return applyQueryOption(q -> q.returnReferences(references));
    }

    public final Builder<PropertiesT> returnMetadata(Metadata... metadata) {
      return applyQueryOption(q -> q.returnMetadata(metadata));
    }

    public final Builder<PropertiesT> returnMetadata(List<Metadata> metadata) {
      return applyQueryOption(q -> q.returnMetadata(metadata));
    }

    private final Builder<PropertiesT> applyQueryOption(
        Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> options) {
      this.queryOptions = ObjectBuilder.partial(this.queryOptions, options);
      return this;
    }

    @Override
    public AsyncPaginator<PropertiesT> build() {
      return new AsyncPaginator<>(this);
    }
  }
}
