package io.weaviate.client6.v1.api.export;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateExportClientAsync {
  private final RestTransport restTransport;

  public WeaviateExportClientAsync(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Start a new export process.
   *
   * @param exportId Export ID. Must be unique for the backend.
   * @param backend  Export storage backend.
   */
  public CompletableFuture<Export> create(String exportId, String backend, FileFormat fileFormat) {
    return create(new CreateExportRequest(CreateExportRequest.ExportCreate.of(exportId, fileFormat), backend));
  }

  /**
   * Start a new export process.
   *
   * @param exportId Export ID. Must be unique for the backend.
   * @param backend  Export storage backend.
   * @param fn       Lambda expression for optional parameters.
   */
  public CompletableFuture<Export> create(String exportId, String backend, FileFormat fileFormat,
      Function<CreateExportRequest.ExportCreate.Builder, ObjectBuilder<CreateExportRequest.ExportCreate>> fn) {
    return create(new CreateExportRequest(CreateExportRequest.ExportCreate.of(exportId, fileFormat, fn), backend));
  }

  /**
   * Start a new export process.
   *
   * @param request Create export request.
   */
  private CompletableFuture<Export> create(CreateExportRequest request) {
    return this.restTransport.performRequestAsync(request, CreateExportRequest._ENDPOINT);
  }

  /**
   * Get export create status.
   *
   * @param exportId Export ID.
   * @param backend  Export storage backend.
   */
  public CompletableFuture<Optional<Export>> getCreateStatus(String exportId, String backend) {
    return this.restTransport.performRequestAsync(
        new GetCreateStatusRequest(exportId, backend), GetCreateStatusRequest._ENDPOINT);
  }

  /**
   * Cancel in-progress export creation.
   *
   * @param exportId Export ID.
   * @param backend  Export storage backend.
   */
  public CompletableFuture<Void> cancel(String exportId, String backend) {
    return this.restTransport.performRequestAsync(new CancelExportRequest(exportId, backend),
        CancelExportRequest._ENDPOINT);
  }
}
