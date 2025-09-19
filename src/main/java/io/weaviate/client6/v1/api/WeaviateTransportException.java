package io.weaviate.client6.v1.api;

/** Exception thrown by the internal transport layer. Usually not retryable. */
public class WeaviateTransportException extends WeaviateException {
  public WeaviateTransportException(String message) {
    super(message);
  }

  public WeaviateTransportException(String message, Throwable cause) {
    super(message, cause);
  }

  public WeaviateTransportException(Throwable cause) {
    super(cause);
  }
}
