package io.weaviate.client.v1.async.rbac.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.AllArgsConstructor;

public class RoleAssigner extends AsyncBaseClient<Void> implements AsyncClientResult<Void> {
  private String user;
  private List<String> roles = new ArrayList<>();

  public RoleAssigner(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public RoleAssigner withUser(String user) {
    this.user = user;
    return this;
  }

  public RoleAssigner witRoles(String... roles) {
    this.roles = Arrays.asList(roles);
    return this;
  }

  /** The API signature for this method is { "roles": [...] } */
  @AllArgsConstructor
  private static class Body {
    public final List<String> roles;
  }

  @Override
  public Future<Result<Void>> run(FutureCallback<Result<Void>> callback) {
    return sendPostRequest(path(), new Body(this.roles), Void.class, callback);
  }

  private String path() {
    return String.format("/authz/users/%s/assign", this.user);
  }
}
