package io.weaviate.client6.v1.api.rbac.roles;

import java.util.Collections;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;
import io.weaviate.client6.v1.internal.rest.UrlEncoder;

public record GetAssignedUsersRequest(String roleName) {
  @SuppressWarnings("unchecked")
  public static final Endpoint<GetAssignedUsersRequest, List<String>> _ENDPOINT = SimpleEndpoint.noBody(
      __ -> "GET",
      request -> "/authz/roles/" + UrlEncoder.encodeValue(request.roleName) + "/users",
      __ -> Collections.emptyMap(),
      (statusCode, response) -> (List<String>) JSON.deserialize(response,
          TypeToken.getParameterized(List.class, String.class)));
}
