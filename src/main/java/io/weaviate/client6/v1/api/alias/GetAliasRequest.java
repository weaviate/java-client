package io.weaviate.client6.v1.api.alias;

import java.util.Collections;
import java.util.Optional;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.OptionalEndpoint;

public record GetAliasRequest(String alias) {
  public final static Endpoint<GetAliasRequest, Optional<Alias>> _ENDPOINT = OptionalEndpoint.noBodyOptional(
      __ -> "GET",
      request -> "/aliases/" + request.alias,
      __ -> Collections.emptyMap(),
      (statusCode, response) -> JSON.deserialize(response, Alias.class));

}
