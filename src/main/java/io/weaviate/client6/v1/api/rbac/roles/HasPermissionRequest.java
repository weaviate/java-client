package io.weaviate.client6.v1.api.rbac.roles;

import java.util.Collections;

import io.weaviate.client6.v1.api.rbac.Permission;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record HasPermissionRequest(String roleName, Permission permission) {
  public static final Endpoint<HasPermissionRequest, Boolean> _ENDPOINT = new SimpleEndpoint<>(
      __ -> "POST",
      request -> "/authz/roles/" + request.roleName + "/has-permission",
      __ -> Collections.emptyMap(),
      request -> JSON.serialize(request.permission),
      (statusCode, response) -> JSON.deserialize(response, Boolean.class));
}
