package io.weaviate.client6.v1.api.export;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record CancelExportRequest(String exportId, String backend) {

  public static Endpoint<CancelExportRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      __ -> "DELETE",
      request -> {
        var cancel = (CancelExportRequest) request;
        return "/export/" + cancel.backend + "/" + cancel.exportId;
      },
      request -> Collections.emptyMap())
      .allowStatus(409);
}
