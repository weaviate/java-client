package io.weaviate.client6.v1.api.collections.pagination;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.weaviate.client6.v1.api.collections.query.FetchObjects;
import io.weaviate.client6.v1.api.collections.query.Filter;
import io.weaviate.client6.v1.api.collections.query.Metadata;
import io.weaviate.client6.v1.api.collections.query.QueryReference;
import io.weaviate.client6.v1.api.collections.query.ReadWeaviateObject;
import io.weaviate.client6.v1.api.collections.query.WeaviateQueryClient;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public class Paginator<PropertiesT> implements Iterable<ReadWeaviateObject<PropertiesT>> {
  static final int DEFAULT_PAGE_SIZE = 100;

  private final WeaviateQueryClient<PropertiesT> query;
  private final Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> queryOptions;
  private final int pageSize;
  private final String cursor;

  @Override
  public Iterator<ReadWeaviateObject<PropertiesT>> iterator() {
    return Spliterators.iterator(spliterator());
  }

  public Stream<ReadWeaviateObject<PropertiesT>> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  public Spliterator<ReadWeaviateObject<PropertiesT>> spliterator() {
    return new CursorSpliterator<PropertiesT>(cursor, pageSize,
        (after, limit) -> {
          var fn = ObjectBuilder.partial(queryOptions, q -> q.after(after).limit(limit));
          try {
            return query.fetchObjects(fn).objects();
          } catch (Exception e) {
            throw PaginationException.after(cursor, pageSize, e);
          }
        });
  }

  public static <T> Paginator<T> of(WeaviateQueryClient<T> query) {
    return of(query, ObjectBuilder.identity());
  }

  public static <T> Paginator<T> of(WeaviateQueryClient<T> query,
      Function<Builder<T>, ObjectBuilder<Paginator<T>>> fn) {
    return fn.apply(new Builder<>(query)).build();
  }

  Paginator(Builder<PropertiesT> builder) {
    this.query = builder.query;
    this.queryOptions = builder.queryOptions;
    this.cursor = builder.cursor;
    this.pageSize = builder.pageSize;
  }

  public static class Builder<T> implements ObjectBuilder<Paginator<T>> {
    private final WeaviateQueryClient<T> query;

    private Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> queryOptions = ObjectBuilder.identity();
    private int pageSize = DEFAULT_PAGE_SIZE;
    private String cursor;

    public Builder(WeaviateQueryClient<T> query) {
      this.query = query;
    }

    // Pagination options -----------------------------------------------------

    /** Set a different page size. Defaults to 100. */
    public Builder<T> pageSize(int pageSize) {
      this.pageSize = pageSize;
      return this;
    }

    /** Set a cursor (object UUID) to start pagination from. */
    public Builder<T> fromCursor(String uuid) {
      this.cursor = uuid;
      return this;
    }

    // Query options ----------------------------------------------------------

    /** Combine several conditions using with an AND operator. */
    public final Builder<T> filters(Filter... filters) {
      return applyQueryOption(q -> q.filters(filters));
    }

    /** Include default vector. */
    public final Builder<T> includeVector() {
      return applyQueryOption(q -> q.includeVector());
    }

    /** Include one or more named vectors in the metadata response. */
    public final Builder<T> includeVector(String... vectors) {
      return applyQueryOption(q -> q.includeVector(vectors));
    }

    /** Include one or more named vectors in the metadata response. */
    public final Builder<T> includeVector(List<String> vectors) {
      return applyQueryOption(q -> q.includeVector(vectors));
    }

    public final Builder<T> returnProperties(String... properties) {
      return applyQueryOption(q -> q.returnProperties(properties));
    }

    public final Builder<T> returnProperties(List<String> properties) {
      return applyQueryOption(q -> q.returnProperties(properties));
    }

    public final Builder<T> returnReferences(QueryReference... references) {
      return applyQueryOption(q -> q.returnReferences(references));
    }

    public final Builder<T> returnReferences(List<QueryReference> references) {
      return applyQueryOption(q -> q.returnReferences(references));
    }

    public final Builder<T> returnMetadata(Metadata... metadata) {
      return applyQueryOption(q -> q.returnMetadata(metadata));
    }

    public final Builder<T> returnMetadata(List<Metadata> metadata) {
      return applyQueryOption(q -> q.returnMetadata(metadata));
    }

    private final Builder<T> applyQueryOption(Function<FetchObjects.Builder, ObjectBuilder<FetchObjects>> options) {
      this.queryOptions = ObjectBuilder.partial(this.queryOptions, options);
      return this;
    }

    @Override
    public Paginator<T> build() {
      return new Paginator<>(this);
    }
  }
}
