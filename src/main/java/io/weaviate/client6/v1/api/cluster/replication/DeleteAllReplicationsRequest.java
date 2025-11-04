package io.weaviate.client6.v1.api.cluster.replication;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record DeleteAllReplicationsRequest() {

  static final Endpoint<Void, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      request -> "DELETE",
      request -> "/replication/replicate",
      __ -> Collections.emptyMap());
}
