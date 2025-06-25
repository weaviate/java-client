package io.weaviate.client6.v1.api.collections;

import java.util.Collections;

import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record CreateCollectionRequest(CollectionConfig collection) {
  public static final Endpoint<CreateCollectionRequest, CollectionConfig> _ENDPOINT = Endpoint.of(
      request -> "POST",
      request -> "/schema/",
      (gson, request) -> JSON.serialize(request.collection),
      request -> Collections.emptyMap(),
      code -> code != HttpStatus.SC_SUCCESS,
      (gson, response) -> JSON.deserialize(response, CollectionConfig.class));
}
