package io.weaviate.client6.v1.api.rbac.roles;

import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.rbac.Permission;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;
import io.weaviate.client6.v1.internal.rest.UrlEncoder;

public record RemovePermissionsRequest(String roleName, List<Permission> permissions) {
  public static final Endpoint<RemovePermissionsRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      __ -> "POST",
      request -> "/authz/roles/" + UrlEncoder.encodeValue(request.roleName) + "/remove-permissions",
      __ -> Collections.emptyMap(),
      request -> JSON.serialize(new Body(request.permissions)));

  /** Request body must be {"permissions": [...]}. */
  private static record Body(@SerializedName("permissions") List<Permission> permissions) {
  }
}
