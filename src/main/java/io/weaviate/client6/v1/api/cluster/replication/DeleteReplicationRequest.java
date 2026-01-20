package io.weaviate.client6.v1.api.cluster.replication;

import java.util.Collections;
import java.util.UUID;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record DeleteReplicationRequest(UUID uuid) {

  static final Endpoint<DeleteReplicationRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      request -> "DELETE",
      request -> "/replication/replicate/" + request.uuid(),
      __ -> Collections.emptyMap());
}
