package io.weaviate.client6.v1.api.rbac.users;

import java.util.Collections;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record GetMyUserRequest() {

  public static final Endpoint<Void, User> _ENDPOINT = SimpleEndpoint.noBody(
      __ -> "GET",
      __ -> "/users/own-info",
      __ -> Collections.emptyMap(),
      (statusCode, response) -> JSON.deserialize(response, User.class));
}
