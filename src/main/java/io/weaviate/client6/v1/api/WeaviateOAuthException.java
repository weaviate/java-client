package io.weaviate.client6.v1.api;

/**
 * Exception throws by the authentication layer if it encountered another
 * exception at any point of obtaining the new token or rotating one.
 */
public class WeaviateOAuthException extends WeaviateException {
  public WeaviateOAuthException(String message) {
    super(message);
  }

  public WeaviateOAuthException(String message, Throwable cause) {
    super(message, cause);
  }

  public WeaviateOAuthException(Throwable cause) {
    super(cause);
  }
}
