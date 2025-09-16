package io.weaviate.client6.v1.api;

/**
 * Exception class thrown by client when the request had reached the
 * server, but the operation did not complete successfully either
 * due to a bad request or a server error.
 */
public class WeaviateApiException extends WeaviateException {
  private final String errorMessage;
  private final Source source;
  private final String endpoint;
  private final Integer httpStatusCode;
  private final io.grpc.Status.Code grpcStatusCode;

  private enum Source {
    HTTP, GRPC;
  };

  public static WeaviateApiException http(String method, String endpoint, int statusCode, String errorMessage) {
    return new WeaviateApiException(method, endpoint, statusCode, errorMessage);
  }

  public static WeaviateApiException gRPC(io.grpc.StatusRuntimeException ex) {
    var status = ex.getStatus();
    return new WeaviateApiException(status.getCode(), status.getDescription());
  }

  private WeaviateApiException(io.grpc.Status.Code code, String errorMessage) {
    super("%s: %s".formatted(code, errorMessage));
    this.source = Source.GRPC;
    this.errorMessage = errorMessage;
    this.grpcStatusCode = code;
    this.endpoint = null;
    this.httpStatusCode = null;
  }

  private WeaviateApiException(String method, String endpoint, int statusCode, String errorMessage) {
    super("HTTP %d: %s %s: %s".formatted(statusCode, method, endpoint, errorMessage));
    this.source = Source.HTTP;
    this.errorMessage = errorMessage;
    this.endpoint = endpoint;
    this.httpStatusCode = statusCode;
    this.grpcStatusCode = null;
  }

  public boolean isGPRC() {
    return source == Source.GRPC;
  }

  public String grpcStatusCode() {
    return grpcStatusCode.toString();
  }

  public boolean isHTTP() {
    return source == Source.HTTP;
  }

  public String endpoint() {
    return endpoint;
  }

  public Integer httpStatusCode() {
    return httpStatusCode;
  }

  public String getError() {
    return errorMessage;
  }
}
