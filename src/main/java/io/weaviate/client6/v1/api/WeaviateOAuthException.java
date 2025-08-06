package io.weaviate.client6.v1.api;

/**
 * Exception class thrown by client API message when the request's reached the
 * server, but the operation did not complete successfully either either due to
 * a bad request or a server error.
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
