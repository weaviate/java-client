package io.weaviate.client6.v1.api.collections;

import java.util.Collections;
import java.util.List;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record ListCollectionRequest() {
  public static final Endpoint<ListCollectionRequest, List<CollectionConfig>> _ENDPOINT = SimpleEndpoint.noBody(
      request -> "GET",
      request -> "/schema",
      request -> Collections.emptyMap(),
      (gson, response) -> JSON.deserialize(response, ListCollectionResponse.class).collections());
}
