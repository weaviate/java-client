package io.weaviate.client6.v1.api.cluster.replication;

import java.util.Collections;
import java.util.UUID;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record CancelReplicationRequest(UUID uuid) {

  static final Endpoint<CancelReplicationRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      request -> "POST",
      request -> "/replication/replicate/" + request.uuid() + "/cancel",
      __ -> Collections.emptyMap());
}
