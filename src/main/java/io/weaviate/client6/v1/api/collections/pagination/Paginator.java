package io.weaviate.client6.v1.api.collections.pagination;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.query.QueryMetadata;
import io.weaviate.client6.v1.api.collections.query.WeaviateQueryClient;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public class Paginator<T> implements Iterable<WeaviateObject<T, Object, QueryMetadata>> {
  private static final int DEFAULT_PAGE_SIZE = 100;

  private final WeaviateQueryClient<T> query;
  private final int pageSize;
  private final String cursor;

  @Override
  public Iterator<WeaviateObject<T, Object, QueryMetadata>> iterator() {
    return Spliterators.iterator(spliterator());
  }

  public Stream<WeaviateObject<T, Object, QueryMetadata>> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  public Spliterator<WeaviateObject<T, Object, QueryMetadata>> spliterator() {
    return new CursorSpliterator<T>(cursor, pageSize,
        (after, limit) -> query.fetchObjects(q -> q.after(after).limit(limit)).objects());
  }

  public static <T> Paginator<T> of(WeaviateQueryClient<T> query) {
    return of(query, ObjectBuilder.identity());
  }

  public static <T> Paginator<T> of(WeaviateQueryClient<T> query,
      Function<Builder<T>, ObjectBuilder<Paginator<T>>> fn) {
    return fn.apply(new Builder<>(query)).build();
  }

  Paginator(Builder<T> builder) {
    this.query = builder.query;
    this.cursor = builder.cursor;
    this.pageSize = builder.pageSize;
  }

  public static class Builder<T> implements ObjectBuilder<Paginator<T>> {
    private final WeaviateQueryClient<T> query;

    int pageSize = DEFAULT_PAGE_SIZE;
    String cursor;

    public Builder(WeaviateQueryClient<T> query) {
      this.query = query;
    }

    public Builder<T> pageSize(int pageSize) {
      this.pageSize = pageSize;
      return this;
    }

    public Builder<T> resumeFrom(String uuid) {
      this.cursor = uuid;
      return this;
    }

    @Override
    public Paginator<T> build() {
      return new Paginator<>(this);
    }
  }

}
