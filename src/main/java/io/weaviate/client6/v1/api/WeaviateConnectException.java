package io.weaviate.client6.v1.api;

/** Exception thrown if the Weaviate instance appears to be offline. */
public class WeaviateConnectException extends WeaviateException {
  public WeaviateConnectException(String message) {
    super(message);
  }

  public WeaviateConnectException(String message, Throwable cause) {
    super(message, cause);
  }

  public WeaviateConnectException(Throwable cause) {
    super(cause);
  }
}
