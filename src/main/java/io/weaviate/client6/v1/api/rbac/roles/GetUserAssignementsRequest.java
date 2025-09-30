package io.weaviate.client6.v1.api.rbac.roles;

import java.util.Collections;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record GetUserAssignementsRequest(String roleName) {
  @SuppressWarnings("unchecked")
  public static final Endpoint<GetUserAssignementsRequest, List<UserAssignment>> _ENDPOINT = SimpleEndpoint.noBody(
      __ -> "GET",
      request -> "/authz/roles/" + request.roleName + "/user-assignments",
      __ -> Collections.emptyMap(),
      (statusCode, response) -> (List<UserAssignment>) JSON.deserialize(response,
          TypeToken.getParameterized(List.class, UserAssignment.class)));
}
