package io.weaviate.client6.v1.api.rbac.roles;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.BooleanEndpoint;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record RoleExistsRequest(String roleName) {
  public static final Endpoint<RoleExistsRequest, Boolean> _ENDPOINT = BooleanEndpoint.noBody(
      __ -> "GET",
      request -> "/authz/roles/" + request.roleName,
      __ -> Collections.emptyMap());
}
