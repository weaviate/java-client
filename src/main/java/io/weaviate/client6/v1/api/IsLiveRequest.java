package io.weaviate.client6.v1.api;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.BooleanEndpoint;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record IsLiveRequest() {
  public static final Endpoint<Void, Boolean> _ENDPOINT = BooleanEndpoint.noBody(
      request -> "GET",
      request -> "/.well-known/live",
      request -> Collections.emptyMap());
}
