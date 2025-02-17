package io.weaviate.client.v1.async.users.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

public class RoleRevoker extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private String user;
  private List<String> roles = new ArrayList<>();

  public RoleRevoker(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public RoleRevoker withUser(String user) {
    this.user = user;
    return this;
  }

  public RoleRevoker witRoles(String... roles) {
    this.roles = Collections.unmodifiableList(Arrays.asList(roles));
    return this;
  }

  /** The API signature for this method is { "roles": [...] } */
  @AllArgsConstructor
  private static class Body {
    public final List<String> roles;
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    return sendPostRequest(path(), new Body(this.roles), callback, Result.voidToBooleanParser());
  }

  private String path() {
    return String.format("/authz/users/%s/revoke", this.user);
  }
}
