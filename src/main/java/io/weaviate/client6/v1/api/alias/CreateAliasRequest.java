package io.weaviate.client6.v1.api.alias;

import java.util.Collections;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record CreateAliasRequest(Alias alias) {
  public final static Endpoint<CreateAliasRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      __ -> "POST",
      __ -> "/aliases/",
      __ -> Collections.emptyMap(),
      request -> JSON.serialize(request.alias));
}
