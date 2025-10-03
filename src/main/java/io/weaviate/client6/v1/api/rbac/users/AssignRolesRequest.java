package io.weaviate.client6.v1.api.rbac.users;

import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;
import io.weaviate.client6.v1.internal.rest.UrlEncoder;

public record AssignRolesRequest(String userId, UserType userType, List<String> roleNames) {

  public static final Endpoint<AssignRolesRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      __ -> "POST",
      request -> "/authz/users/" + UrlEncoder.encodeValue(request.userId) + "/assign",
      request -> Collections.emptyMap(),
      request -> JSON.serialize(new Body(request.roleNames, request.userType)));

  /** Request body should be {"roles": [...], "userType": "<userType>"} */
  private static record Body(
      @SerializedName("roles") List<String> roleNames,
      @SerializedName("userType") UserType userType) {
  }
}
