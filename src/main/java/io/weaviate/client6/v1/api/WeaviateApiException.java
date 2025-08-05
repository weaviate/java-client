package io.weaviate.client6.v1.api;

/**
 * Exception class thrown by client API message when the request's reached the
 * server, but the operation did not complete successfully either either due to
 * a bad request or a server error.
 */
public class WeaviateApiException extends RuntimeException {
  private final String errorMessage;
  private final Source source;
  private final String endpoint;
  private final Integer statusCode;
  private final String grpcStatus;

  private enum Source {
    HTTP, GRPC;
  };

  public static WeaviateApiException http(String method, String endpoint, int statusCode, String errorMessage) {
    return new WeaviateApiException(method, endpoint, statusCode, errorMessage);
  }

  public static WeaviateApiException gRPC(io.grpc.StatusRuntimeException ex) {
    var status = ex.getStatus();
    return new WeaviateApiException(status.getCode().toString(), status.getDescription());
  }

  private WeaviateApiException(String status, String errorMessage) {
    super("%s: %s".formatted(status, errorMessage));
    this.source = Source.GRPC;
    this.errorMessage = errorMessage;
    this.grpcStatus = status;
    this.endpoint = null;
    this.statusCode = null;
  }

  private WeaviateApiException(String method, String endpoint, int statusCode, String errorMessage) {
    super("HTTP %d: %s %s: %s".formatted(statusCode, method, endpoint, errorMessage));
    this.source = Source.HTTP;
    this.errorMessage = errorMessage;
    this.endpoint = endpoint;
    this.statusCode = statusCode;
    this.grpcStatus = null;
  }

  public String endpoint() {
    return endpoint;
  }

  public Integer statusCode() {
    return statusCode;
  }
}
