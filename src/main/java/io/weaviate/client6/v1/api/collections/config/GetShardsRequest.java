package io.weaviate.client6.v1.api.collections.config;

import java.util.Collections;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record GetShardsRequest(String collectionName) {

  @SuppressWarnings("unchecked")
  public static final Endpoint<GetShardsRequest, List<Shard>> _ENDPOINT = SimpleEndpoint.noBody(
      request -> "GET",
      request -> "/schema/" + request.collectionName + "/shards", // TODO: tenant support
      request -> Collections.emptyMap(),
      (statusCode, response) -> (List<Shard>) JSON.deserialize(response, TypeToken.getParameterized(
          List.class, Shard.class)));
}
