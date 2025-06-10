package io.weaviate.client6.v1.internal.json;

public abstract class JsonDelegate<T> {
  /**
   * No-op constructor enforces subclasses to override it and populate their
   * instances with model values.
   */
  protected JsonDelegate(T model) {
  }

  public abstract T toModel();
}
