package io.weaviate.client.v1.rbac.api;

import java.util.Optional;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.rbac.model.Role;

public class RoleGetter extends BaseClient<WeaviateRole> implements ClientResult<Role> {
  private String name;

  public RoleGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public RoleGetter withName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public Result<Role> run() {
    Response<WeaviateRole> resp = sendGetRequest("/authz/roles/" + this.name, WeaviateRole.class);
    Role role = Optional.ofNullable(resp.getBody()).map(WeaviateRole::toRole).orElse(null);
    return new Result<Role>(resp.getStatusCode(), role, resp.getErrors());
  }
}
