package io.weaviate.client6.v1.api;

/**
 * Exception class thrown by client API message when the request's reached the
 * server, but the operation did not complete successfully either either due to
 * a bad request or a server error.
 */
public class WeaviateApiException extends RuntimeException {
  // TODO: rather than storing bare values (status code, response body),
  // store "Response" object and provide accessors to .status(), .error().

  private final String endpoint;
}
