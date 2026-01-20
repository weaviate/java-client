package io.weaviate.client6.v1.api.collections;

import java.util.Collections;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record CreateCollectionRequest(CollectionConfig collection) {
  public static final Endpoint<CreateCollectionRequest, CollectionConfig> _ENDPOINT = new SimpleEndpoint<>(
      request -> "POST",
      request -> "/schema/",
      request -> Collections.emptyMap(),
      request -> JSON.serialize(request.collection),
      (statusCode, response) -> JSON.deserialize(response, CollectionConfig.class));
}
