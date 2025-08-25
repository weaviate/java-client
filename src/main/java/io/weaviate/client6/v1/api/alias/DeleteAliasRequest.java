package io.weaviate.client6.v1.api.alias;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record DeleteAliasRequest(String alias) {
  public final static Endpoint<DeleteAliasRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      __ -> "DELETE",
      request -> "/aliases/" + request.alias,
      __ -> Collections.emptyMap());
}
