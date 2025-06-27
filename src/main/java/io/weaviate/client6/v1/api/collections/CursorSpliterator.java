package io.weaviate.client6.v1.api.collections;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import io.weaviate.client6.v1.api.collections.query.QueryMetadata;

class CursorSpliterator<T> implements Spliterator<WeaviateObject<T, Object, QueryMetadata>> {
  private final int batchSize;
  private final BiFunction<String, Integer, List<WeaviateObject<T, Object, QueryMetadata>>> fetch;

  // Spliterators do not promise thread-safety, so there's no mechanism
  // to protect access to its internal state.
  private String cursor;
  private Iterator<WeaviateObject<T, Object, QueryMetadata>> currentPage = Collections.emptyIterator();

  public CursorSpliterator(int batchSize,
      BiFunction<String, Integer, List<WeaviateObject<T, Object, QueryMetadata>>> fetch) {
    this.batchSize = batchSize;
    this.fetch = fetch;
  }

  @Override
  public boolean tryAdvance(Consumer<? super WeaviateObject<T, Object, QueryMetadata>> action) {
    // Happy path: there are remaining objects in the current page.
    if (currentPage.hasNext()) {
      action.accept(currentPage.next());
      return true;
    }

    // It's OK for the cursor to be null, because it's String (object).
    var nextPage = fetch.apply(cursor, batchSize);
    if (nextPage.isEmpty()) {
      return false;
    }
    cursor = nextPage.get(nextPage.size() - 1).metadata().uuid();
    currentPage = nextPage.iterator();
    return tryAdvance(action);
  }

  @Override
  public Spliterator<WeaviateObject<T, Object, QueryMetadata>> trySplit() {
    // Do not support splitting just now;
    return null;
  }

  @Override
  public long estimateSize() {
    // CursorSpliterator does not have SIZED characteristic, so this is our
    // best-effort estimate. The number of objects in the db is unbounded.
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return ORDERED | NONNULL;
  }
}
