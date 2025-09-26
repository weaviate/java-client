package io.weaviate.client6.v1.api.collections.pagination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.QueryMetadata;

public final class AsyncPage<PropertiesT> implements Iterable<WeaviateObject<PropertiesT, Object, QueryMetadata>> {

  private final int pageSize;
  private final BiFunction<String, Integer, CompletableFuture<List<WeaviateObject<PropertiesT, Object, QueryMetadata>>>> fetch;

  private final String cursor;
  private List<WeaviateObject<PropertiesT, Object, QueryMetadata>> currentPage = new ArrayList<>();

  AsyncPage(String cursor, int pageSize,
      BiFunction<String, Integer, CompletableFuture<List<WeaviateObject<PropertiesT, Object, QueryMetadata>>>> fetch) {
    this.cursor = cursor;
    this.pageSize = pageSize;
    this.fetch = fetch;
  }

  AsyncPage(String cursor, int pageSize,
      BiFunction<String, Integer, CompletableFuture<List<WeaviateObject<PropertiesT, Object, QueryMetadata>>>> fetch,
      List<WeaviateObject<PropertiesT, Object, QueryMetadata>> currentPage) {
    this(cursor, pageSize, fetch);
    this.currentPage = Collections.unmodifiableList(currentPage);
  }

  List<WeaviateObject<PropertiesT, Object, QueryMetadata>> items() {
    return currentPage;
  }

  public boolean isEmpty() {
    return this.currentPage.isEmpty();
  }

  /**
   * Fetch an {@link AsyncPage} containing the next {@code pageSize} results
   * and advance the cursor.
   *
   * <p>
   * The returned stage may complete exceptionally in case the underlying
   * query fails. Callers are advised to use exception-aware
   * {@link CompletableFuture#handle} to process page results.
   */
  public CompletableFuture<AsyncPage<PropertiesT>> fetchNextPage() {
    return fetch.apply(cursor, pageSize)
        .thenApply(nextPage -> {
          if (nextPage.isEmpty()) {
            return new AsyncPage<>(null, pageSize, fetch, nextPage);
          }
          var last = nextPage.get(nextPage.size() - 1);
          var nextCursor = last.uuid();
          // The cursor can only be null on the first iteration.
          // If it is null after the first iteration it is
          // because we haven't requested Metadata.UUID, in which
          // case pagination will continue to run unbounded.
          if (nextCursor == null) {
            throw new IllegalStateException("page cursor is null");
          }
          return new AsyncPage<>(nextCursor, pageSize, fetch, nextPage);
        });
  }

  @Override
  public Iterator<WeaviateObject<PropertiesT, Object, QueryMetadata>> iterator() {
    return currentPage.iterator();
  }
}
