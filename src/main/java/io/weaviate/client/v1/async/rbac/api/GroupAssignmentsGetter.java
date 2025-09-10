package io.weaviate.client.v1.async.rbac.api;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.rbac.model.GroupAssignment;

public class GroupAssignmentsGetter extends AsyncBaseClient<List<GroupAssignment>>
    implements AsyncClientResult<List<GroupAssignment>> {
  private String role;

  public GroupAssignmentsGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public GroupAssignmentsGetter withRole(String role) {
    this.role = role;
    return this;
  }

  @Override
  public Future<Result<List<GroupAssignment>>> run(FutureCallback<Result<List<GroupAssignment>>> callback) {
    return sendGetRequest(path(), callback, Result.arrayToListParser(GroupAssignment[].class));
  }

  private String path() {
    return String.format("/authz/roles/%s/group-assignments", this.role);
  }
}
