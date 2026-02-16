package io.weaviate.client6.v1.api.backup;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record CancelBackupRestoreRequest(String backupId, String backend) {

  public static Endpoint<CancelBackupRestoreRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      request -> "DELETE",
      request -> "/backups/" + request.backend + "/" + request.backupId + "/restore",
      request -> Collections.emptyMap());
}
