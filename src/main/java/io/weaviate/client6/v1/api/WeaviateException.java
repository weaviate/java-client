package io.weaviate.client6.v1.api;

/**
 * WeaviateException is the base class for other library exceptions
 * to provide an ergonomic way of handling any Weaviate-related exceptions.
 *
 * <p>
 * Some parts of the API may still throw other standard exceptions, like
 * {@link java.io.IOException} or {@link java.lang.IllegalArgumentException},
 * which will not be wrapped into a WeaviateException.
 *
 * <p>
 * Usage:
 *
 * <pre>{@code
 *  var thigns = client.collections.use("Things");
 *  try {
 *    things.paginate(...)
 *    things.query.bm25(...);
 *    things.aggregate.overAll(...);
 *  } catch (WeaviateException e) {
 *    System.out.println(e);
 *  }
 * }</pre>
 */
public abstract class WeaviateException extends RuntimeException {
  public WeaviateException(String message) {
    super(message);
  }

  public WeaviateException(Throwable cause) {
    super(cause);
  }

  public WeaviateException(String message, Throwable cause) {
    super(message, cause);
  }
}
