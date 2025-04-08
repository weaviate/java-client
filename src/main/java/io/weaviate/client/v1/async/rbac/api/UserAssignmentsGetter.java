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
import io.weaviate.client.v1.rbac.model.UserAssignment;

public class UserAssignmentsGetter extends AsyncBaseClient<List<UserAssignment>>
    implements AsyncClientResult<List<UserAssignment>> {
  private String role;

  public UserAssignmentsGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public UserAssignmentsGetter withRole(String role) {
    this.role = role;
    return this;
  }

  @Override
  public Future<Result<List<UserAssignment>>> run(FutureCallback<Result<List<UserAssignment>>> callback) {
    return sendGetRequest(path(), callback, Result.arrayToListParser(UserAssignment[].class));
  }

  private String path() {
    return String.format("/authz/roles/%s/user-assignments", this.role);
  }
}
