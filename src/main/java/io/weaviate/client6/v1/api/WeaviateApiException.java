package io.weaviate.client6.v1.api;

/**
 * Exception class thrown by client API message when the request's reached the
 * server, but the operation did not complete successfully either either due to
 * a bad request or a server error.
 */
public class WeaviateApiException extends RuntimeException {
  private final String endpoint;
  private final int statusCode;

  public WeaviateApiException(String method, String endpoint, int statusCode, String errorMessage) {
    super("%s %s: %s".formatted(method, endpoint, errorMessage));
    this.endpoint = endpoint;
    this.statusCode = statusCode;
  }

  public String endpoint() {
    return endpoint;
  }

  public int statusCode() {
    return statusCode;
  }
}
