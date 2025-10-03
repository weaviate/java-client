package io.weaviate.client6.v1.api.rbac.users;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;
import io.weaviate.client6.v1.internal.rest.UrlEncoder;

public record CreateDbUserRequest(String userId) {

  public static final Endpoint<CreateDbUserRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      __ -> "POST",
      request -> "/users/db/" + UrlEncoder.encodeValue(request.userId),
      request -> Collections.emptyMap());
}
