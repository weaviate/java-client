package io.weaviate.client6.v1.api.collections.pagination;

import io.weaviate.client6.v1.api.WeaviateException;

/**
 * WeaviatePaginationException is thrown then the client encouters an exception
 * while fetching the next page. This exception preserves the original exception
 * (see {@link #getCause} and the information about the last cursor and page
 * size used (see {@link #cursor()} and {@link #pageSize()} respectively).
 */
public class PaginationException extends WeaviateException {
  private final String cursor;
  private final int pageSize;

  public static PaginationException after(String cursor, int pageSize, Throwable cause) {
    return new PaginationException(cursor, pageSize, cause);
  }

  private PaginationException(String cursor, int pageSize, Throwable cause) {
    super("fetch next page, page_size=%d cursor=%s".formatted(pageSize, cursor), cause);
    this.cursor = cursor;
    this.pageSize = pageSize;
  }

  /** A null-cursor means the error happened while fetching the first page. */
  public String cursor() {
    return cursor;
  }

  public int pageSize() {
    return pageSize;
  }
}
