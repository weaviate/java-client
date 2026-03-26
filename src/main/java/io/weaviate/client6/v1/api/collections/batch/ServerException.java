package io.weaviate.client6.v1.api.collections.batch;

import io.weaviate.client6.v1.api.WeaviateException;

/**
 * ServerException carries an error message the server returns in
 * {@link Event.Results}. This is a retriable exception.
 */
public class ServerException extends WeaviateException {
  ServerException(String message) {
    super(message);
  }
}
