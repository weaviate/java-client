package io.weaviate.client6.v1.api.collections;

import java.util.Collections;
import java.util.List;

import org.apache.hc.core5.http.HttpStatus;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record ListCollectionRequest() {
  public static final Endpoint<ListCollectionRequest, List<WeaviateCollection>> _ENDPOINT = Endpoint.of(
      request -> "GET",
      request -> "/schema",
      (gson, request) -> null,
      request -> Collections.emptyMap(),
      code -> code != HttpStatus.SC_SUCCESS,
      (gson, response) -> JSON.deserialize(response, ListCollectionResponse.class).collections());
}
