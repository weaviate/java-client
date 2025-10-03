package io.weaviate.client6.v1.api.backup;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record CancelBackupRequest(String backupId, String backend) {

  public static Endpoint<CancelBackupRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      request -> "DELETE",
      request -> "/backups/" + request.backend + "/" + request.backupId,
      request -> Collections.emptyMap());
}
