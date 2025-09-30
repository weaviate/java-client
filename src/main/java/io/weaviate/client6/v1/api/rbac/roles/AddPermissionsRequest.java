package io.weaviate.client6.v1.api.rbac.roles;

import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.rbac.Permission;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record AddPermissionsRequest(String roleName, List<Permission> permissions) {
  public static final Endpoint<AddPermissionsRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      __ -> "POST",
      request -> "/authz/roles/" + request.roleName + "/add-permissions",
      __ -> Collections.emptyMap(),
      request -> JSON.serialize(new Body(request.permissions)));

  /** Request body must be {"permissions": [...]}. */
  private static record Body(@SerializedName("permissions") List<Permission> permissions) {
  }
}
