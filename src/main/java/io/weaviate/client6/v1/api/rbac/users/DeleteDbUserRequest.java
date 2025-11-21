package io.weaviate.client6.v1.api.rbac.users;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.BooleanEndpoint;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.UrlEncoder;

public record DeleteDbUserRequest(String userId) {

  public static final Endpoint<DeleteDbUserRequest, Boolean> _ENDPOINT = BooleanEndpoint.noBody(
      __ -> "DELETE",
      request -> "/users/db/" + UrlEncoder.encodeValue(request.userId),
      request -> Collections.emptyMap());
}
