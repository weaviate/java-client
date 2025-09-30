package io.weaviate.client6.v1.api.rbac.roles;

import java.util.Collections;

import io.weaviate.client6.v1.api.rbac.Role;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record GetRoleRequest(String roleName) {
  public static final Endpoint<GetRoleRequest, Role> _ENDPOINT = SimpleEndpoint.noBody(
      __ -> "GET",
      request -> "/authz/roles/" + request.roleName,
      __ -> Collections.emptyMap(),
      (statusCode, response) -> JSON.deserialize(response, Role.class));
}
