package io.weaviate.client6.v1.api.rbac.roles;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record DeleteRoleRequest(String roleName) {
  public static final Endpoint<DeleteRoleRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      __ -> "DELETE",
      request -> "/authz/roles/" + request.roleName,
      __ -> Collections.emptyMap());
}
