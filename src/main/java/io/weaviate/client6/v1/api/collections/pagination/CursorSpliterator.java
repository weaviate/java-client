package io.weaviate.client6.v1.api.collections.pagination;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import io.weaviate.client6.v1.api.collections.query.ReadWeaviateObject;

public class CursorSpliterator<PropertiesT> implements Spliterator<ReadWeaviateObject<PropertiesT>> {
  private final int pageSize;
  private final BiFunction<String, Integer, List<ReadWeaviateObject<PropertiesT>>> fetch;

  // Spliterators do not promise thread-safety, so there's no mechanism
  // to protect access to its internal state.
  private String cursor;
  private Iterator<ReadWeaviateObject<PropertiesT>> currentPage = Collections.emptyIterator();

  public CursorSpliterator(String cursor, int pageSize,
      BiFunction<String, Integer, List<ReadWeaviateObject<PropertiesT>>> fetch) {
    this.cursor = cursor;
    this.pageSize = pageSize;
    this.fetch = fetch;
  }

  @Override
  public boolean tryAdvance(Consumer<? super ReadWeaviateObject<PropertiesT>> action) {
    // Happy path: there are remaining objects in the current page.
    if (currentPage.hasNext()) {
      action.accept(currentPage.next());
      return true;
    }

    // It's OK for the cursor to be null, because it's String (object).
    var nextPage = fetch.apply(cursor, pageSize);
    if (nextPage.isEmpty()) {
      return false;
    }
    cursor = nextPage.get(nextPage.size() - 1).uuid();

    // The cursor can only be null on the first iteration.
    // If it is null after the first iteration it is
    // because we haven't requested Metadata.UUID, in which
    // case pagination will continue to run unbounded.
    if (cursor == null) {
      throw new IllegalStateException("page cursor is null");
    }

    currentPage = nextPage.iterator();
    return tryAdvance(action);
  }

  @Override
  public Spliterator<ReadWeaviateObject<PropertiesT>> trySplit() {
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
    return ORDERED | DISTINCT | NONNULL;
  }
}
