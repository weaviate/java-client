package io.weaviate.client6.v1.api.alias;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.BooleanEndpoint;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record DeleteAliasRequest(String alias) {
  public final static Endpoint<DeleteAliasRequest, Boolean> _ENDPOINT = BooleanEndpoint.noBody(
      __ -> "DELETE",
      request -> "/aliases/" + request.alias,
      __ -> Collections.emptyMap());
}
