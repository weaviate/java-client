package io.weaviate.client6.v1.api.export;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateExportClient {
  private final RestTransport restTransport;

  public WeaviateExportClient(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Start a new export process.
   *
   * @param exportId Export ID. Must be unique for the backend.
   * @param backend  Export storage backend.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Export create(String exportId, String backend) throws IOException {
    return create(new CreateExportRequest(CreateExportRequest.ExportCreate.of(exportId), backend));
  }

  /**
   * Start a new export process.
   *
   * @param exportId Export ID. Must be unique for the backend.
   * @param backend  Export storage backend.
   * @param fn       Lambda expression for optional parameters.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Export create(String exportId, String backend,
      Function<CreateExportRequest.ExportCreate.Builder, ObjectBuilder<CreateExportRequest.ExportCreate>> fn)
      throws IOException {
    return create(new CreateExportRequest(CreateExportRequest.ExportCreate.of(exportId, fn), backend));
  }

  /**
   * Start a new export process.
   *
   * @param request Create export request.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Export create(CreateExportRequest request) throws IOException {
    return this.restTransport.performRequest(request, CreateExportRequest._ENDPOINT);
  }

  /**
   * Get export create status.
   *
   * @param exportId Export ID.
   * @param backend  Export storage backend.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public Optional<Export> getCreateStatus(String exportId, String backend) throws IOException {
    return this.restTransport.performRequest(
        new GetCreateStatusRequest(exportId, backend), GetCreateStatusRequest._ENDPOINT);
  }

  /**
   * Cancel in-progress export.
   *
   * @param exportId Export ID.
   * @param backend  Export storage backend.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public void cancel(String exportId, String backend) throws IOException {
    this.restTransport.performRequest(new CancelExportRequest(exportId, backend), CancelExportRequest._ENDPOINT);
  }
}
