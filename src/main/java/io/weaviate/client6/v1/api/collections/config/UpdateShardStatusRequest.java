package io.weaviate.client6.v1.api.collections.config;

import java.util.Collections;
import java.util.Map;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record UpdateShardStatusRequest(String collection, String shard, ShardStatus status) {
  public static final Endpoint<UpdateShardStatusRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      request -> "PUT",
      request -> "/schema/" + request.collection + "/shards/" + request.shard,
      request -> Collections.emptyMap(),
      request -> JSON.serialize(Map.of("status", request.status)));
}
