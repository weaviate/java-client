package io.weaviate.client6.v1.api.collections.config;

import java.util.Collections;
import java.util.List;

import org.apache.hc.core5.http.HttpStatus;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record GetShardsRequest(String collectionName) {

  @SuppressWarnings("unchecked")
  public static final Endpoint<GetShardsRequest, List<Shard>> _ENDPOINT = Endpoint.of(
      request -> "GET",
      request -> "/schema/" + request.collectionName + "/shards", // TODO: tenant support
      (gson, request) -> null,
      request -> Collections.emptyMap(),
      code -> code != HttpStatus.SC_SUCCESS,
      (gson, response) -> (List<Shard>) JSON.deserialize(response, TypeToken.getParameterized(
          List.class, Shard.class)));
}
