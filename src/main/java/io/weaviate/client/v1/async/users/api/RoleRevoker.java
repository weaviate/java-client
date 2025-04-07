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
  private String userId;
  private List<String> roles = new ArrayList<>();

  private final String _userType;

  public RoleRevoker(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    this(httpClient, config, tokenProvider, null);
  }

  public RoleRevoker(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider,
      String userType) {
    super(httpClient, config, tokenProvider);
    this._userType = userType;
  }

  public RoleRevoker withUserId(String id) {
    this.userId = id;
    return this;
  }

  public RoleRevoker witRoles(String... roles) {
    this.roles = Collections.unmodifiableList(Arrays.asList(roles));
    return this;
  }

  /** The API signature for this method is { "roles": [...] } */
  @AllArgsConstructor
  private class Body {
    final String userType = _userType; // always inherit from the outer class
    final List<String> roles;
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    return sendPostRequest(path(), new Body(this.roles), callback, Result.voidToBooleanParser());
  }

  private String path() {
    return String.format("/authz/users/%s/revoke", this.userId);
  }
}
