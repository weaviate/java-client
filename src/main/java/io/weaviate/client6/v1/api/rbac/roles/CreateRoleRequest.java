package io.weaviate.client6.v1.api.rbac.roles;

import java.util.Collections;

import io.weaviate.client6.v1.api.rbac.Role;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record CreateRoleRequest(Role role) {
  public static final Endpoint<CreateRoleRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      __ -> "POST",
      __ -> "/authz/roles",
      __ -> Collections.emptyMap(),
      request -> JSON.serialize(request.role));
}
