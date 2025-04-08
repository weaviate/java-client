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
import io.weaviate.client.v1.rbac.model.UserAssignment;

public class UserAssignmentsGetter extends BaseClient<UserAssignment[]> implements ClientResult<List<UserAssignment>> {
  private String role;

  public UserAssignmentsGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public UserAssignmentsGetter withRole(String role) {
    this.role = role;
    return this;
  }

  @Override
  public Result<List<UserAssignment>> run() {
    Response<UserAssignment[]> resp = sendGetRequest(path(), UserAssignment[].class);
    List<UserAssignment> roles = Optional.ofNullable(resp.getBody())
        .map(Arrays::asList).orElse(new ArrayList<>());
    return new Result<>(resp.getStatusCode(), roles, resp.getErrors());
  }

  private String path() {
    return String.format("/authz/roles/%s/user-assignments", this.role);
  }
}
