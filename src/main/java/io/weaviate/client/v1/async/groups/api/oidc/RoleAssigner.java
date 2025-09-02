package io.weaviate.client.v1.async.groups.api.oidc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import lombok.AllArgsConstructor;

public class RoleAssigner extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private String groupId;
  private List<String> roles = new ArrayList<>();

  public RoleAssigner(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
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
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    return sendPostRequest(path(), new Body(this.roles), callback,
        Result.voidToBooleanParser());
  }

  private String path() {
    return String.format("/authz/groups/%s/assign", _groupId());
  }
}
