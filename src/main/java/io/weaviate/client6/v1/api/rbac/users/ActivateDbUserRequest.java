package io.weaviate.client6.v1.api.rbac.users;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.BooleanEndpoint;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.UrlEncoder;

public record ActivateDbUserRequest(String userId) {

  public static final Endpoint<ActivateDbUserRequest, Boolean> _ENDPOINT = BooleanEndpoint.noBody(
      __ -> "POST",
      request -> "/users/db/" + UrlEncoder.encodeValue(((ActivateDbUserRequest) request).userId) + "/activate",
      request -> Collections.emptyMap())
      .allowStatus(409);
}
