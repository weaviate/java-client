package io.weaviate.client6.v1.api.collections.config;

import java.util.Collections;
import java.util.Map;

import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record UpdateShardStatusRequest(String collection, String shard, ShardStatus status) {
  public static final Endpoint<UpdateShardStatusRequest, Void> _ENDPOINT = Endpoint.of(
      request -> "PUT",
      request -> "/schema/" + request.collection + "/shards/" + request.shard,
      (gson, request) -> JSON.serialize(Map.of("status", request.status)),
      request -> Collections.emptyMap(),
      code -> code != HttpStatus.SC_SUCCESS,
      (gson, response) -> null);
}
