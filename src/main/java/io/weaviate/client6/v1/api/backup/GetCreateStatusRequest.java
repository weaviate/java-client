package io.weaviate.client6.v1.api.backup;

import java.util.Collections;
import java.util.Optional;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.OptionalEndpoint;

public record GetCreateStatusRequest(String backupId, String backend) {
  public static final Endpoint<GetCreateStatusRequest, Optional<Backup>> _ENDPOINT = OptionalEndpoint.noBodyOptional(
      request -> "GET",
      request -> "/backups/" + request.backend + "/" + request.backupId,
      request -> Collections.emptyMap(),
      Backup.class);
}
