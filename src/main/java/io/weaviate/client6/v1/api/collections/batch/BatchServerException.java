package io.weaviate.client6.v1.api.collections.batch;

import io.weaviate.client6.v1.api.WeaviateException;

public class BatchServerException extends WeaviateException {

  public BatchServerException(String message) {
    super(message);
  }
}
