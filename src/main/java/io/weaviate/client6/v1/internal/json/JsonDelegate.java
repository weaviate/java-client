package io.weaviate.client6.v1.internal.json;

public abstract class JsonDelegate<T> {
  protected JsonDelegate(T model) {
  }

  public abstract T toModel();
}
