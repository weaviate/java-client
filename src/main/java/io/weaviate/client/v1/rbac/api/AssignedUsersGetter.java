package io.weaviate.client.v1.rbac.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class AssignedUsersGetter extends BaseClient<String[]> implements ClientResult<List<String>> {
  private String role;

  public AssignedUsersGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public AssignedUsersGetter withRole(String role) {
    this.role = role;
    return this;
  }

  @Override
  public Result<List<String>> run() {
    Response<String[]> resp = sendGetRequest(path(), String[].class);
    List<String> roles = Optional.ofNullable(resp.getBody())
        .map(Arrays::asList)
        .orElse(new ArrayList<>());
    return new Result<>(resp.getStatusCode(), roles, resp.getErrors());
  }

  private String path() {
    return String.format("/authz/roles/%s/users", this.role);
  }
}
