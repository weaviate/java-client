package io.weaviate.client6.v1.api.collections.pagination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.QueryMetadata;

public class AsyncResultSet<PropertiesT> implements Iterable<WeaviateObject<PropertiesT, Object, QueryMetadata>> {

  private final int pageSize;
  private final String cursor;
  private final BiFunction<String, Integer, CompletableFuture<List<WeaviateObject<PropertiesT, Object, QueryMetadata>>>> fetch;

  private List<WeaviateObject<PropertiesT, Object, QueryMetadata>> currentPage = new ArrayList<>();

  AsyncResultSet(String cursor, int pageSize,
      BiFunction<String, Integer, CompletableFuture<List<WeaviateObject<PropertiesT, Object, QueryMetadata>>>> fetch) {
    this.cursor = cursor;
    this.pageSize = pageSize;
    this.fetch = fetch;
  }

  AsyncResultSet(String cursor, int pageSize,
      BiFunction<String, Integer, CompletableFuture<List<WeaviateObject<PropertiesT, Object, QueryMetadata>>>> fetch,
      List<WeaviateObject<PropertiesT, Object, QueryMetadata>> currentPage) {
    this(cursor, pageSize, fetch);
    this.currentPage = Collections.unmodifiableList(currentPage);
  }

  List<WeaviateObject<PropertiesT, Object, QueryMetadata>> currentPage() {
    return currentPage;
  }

  public boolean isEmpty() {
    return this.currentPage.isEmpty();
  }

  public CompletableFuture<AsyncResultSet<PropertiesT>> fetchNextPage() {
    return fetch.apply(cursor, pageSize)
        .thenApply(nextPage -> {
          if (nextPage.isEmpty()) {
            return new AsyncResultSet<>(null, pageSize, fetch, nextPage);
          }
          var last = nextPage.get(nextPage.size() - 1);
          return new AsyncResultSet<>(last.metadata().uuid(), pageSize, fetch, nextPage);
        });
  }

  @Override
  public Iterator<WeaviateObject<PropertiesT, Object, QueryMetadata>> iterator() {
    return currentPage.iterator();
  }
}
