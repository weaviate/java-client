package io.weaviate.client.v1.rbac.api;

import java.util.List;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.rbac.model.GroupAssignment;

public class GroupAssignmentsGetter extends BaseClient<GroupAssignment[]>
    implements ClientResult<List<GroupAssignment>> {
  private String role;

  public GroupAssignmentsGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public GroupAssignmentsGetter withRole(String role) {
    this.role = role;
    return this;
  }

  @Override
  public Result<List<GroupAssignment>> run() {
    return Result.toList(sendGetRequest(path(), GroupAssignment[].class));
  }

  private String path() {
    return String.format("/authz/roles/%s/group-assignments", this.role);
  }
}
