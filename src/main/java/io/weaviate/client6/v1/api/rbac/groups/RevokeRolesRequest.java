package io.weaviate.client6.v1.api.rbac.groups;

import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;
import io.weaviate.client6.v1.internal.rest.UrlEncoder;

public record RevokeRolesRequest(String groupId, List<String> roleNames) {

  public static final Endpoint<RevokeRolesRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      __ -> "POST",
      request -> "/authz/groups/" + UrlEncoder.encodeValue(request.groupId) + "/revoke",
      request -> Collections.emptyMap(),
      request -> JSON.serialize(new Body(request.roleNames, GroupType.OIDC)));

  /** Request body should be {"roles": [...], "groupType": "oidc"} */
  private static record Body(
      @SerializedName("roles") List<String> roleNames,
      @SerializedName("groupType") GroupType groupType) {
  }
}
