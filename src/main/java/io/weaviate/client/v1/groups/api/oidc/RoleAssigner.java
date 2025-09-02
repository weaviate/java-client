package io.weaviate.client.v1.groups.api.oidc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.UrlEncoder;
import lombok.AllArgsConstructor;

public class RoleAssigner extends BaseClient<Void> implements ClientResult<Boolean> {
  private String groupId;
  private List<String> roles = new ArrayList<>();

  public RoleAssigner(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public RoleAssigner withGroupId(String id) {
    this.groupId = id;
    return this;
  }

  public RoleAssigner witRoles(String... roles) {
    this.roles = Arrays.asList(roles);
    return this;
  }

  private String _groupId() {
    return UrlEncoder.encode(this.groupId);
  }

  /** The API signature for this method is { "roles": [...] } */
  @AllArgsConstructor
  private class Body {
    @SerializedName("roles")
    final List<String> roles;
    @SerializedName("groupType")
    final String groupType = "oidc";
  }

  @Override
  public Result<Boolean> run() {
    return Result.voidToBoolean(sendPostRequest(path(), new Body(this.roles), Void.class));
  }

  private String path() {
    return String.format("/authz/groups/%s/assign", _groupId());
  }
}
