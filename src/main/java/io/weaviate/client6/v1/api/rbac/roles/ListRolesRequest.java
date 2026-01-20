package io.weaviate.client6.v1.api.rbac.roles;

import java.util.Collections;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.api.rbac.Role;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record ListRolesRequest() {
  @SuppressWarnings("unchecked")
  public static final Endpoint<Void, List<Role>> _ENDPOINT = SimpleEndpoint.noBody(
      __ -> "GET",
      __ -> "/authz/roles",
      __ -> Collections.emptyMap(),
      (statusCode, response) -> (List<Role>) JSON.deserialize(response,
          TypeToken.getParameterized(List.class, Role.class)));
}
