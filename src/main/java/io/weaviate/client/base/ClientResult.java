package io.weaviate.client.base;

public interface ClientResult<T> {
  Result<T> run();
}
