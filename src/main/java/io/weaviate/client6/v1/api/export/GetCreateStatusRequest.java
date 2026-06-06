package io.weaviate.client6.v1.api.export;

import java.util.Collections;
import java.util.Optional;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.OptionalEndpoint;

public record GetCreateStatusRequest(String exportId, String backend) {
  public static final Endpoint<GetCreateStatusRequest, Optional<Export>> _ENDPOINT = OptionalEndpoint.noBodyOptional(
      request -> "GET",
      request -> "/export/" + request.backend + "/" + request.exportId,
      request -> Collections.emptyMap(),
      Export.class);
}
