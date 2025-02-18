package io.weaviate.client.v1.async.rbac.api;

import java.util.Optional;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.async.ResponseParser;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.rbac.api.WeaviateRole;
import io.weaviate.client.v1.rbac.model.Role;

public class RoleGetter extends AsyncBaseClient<Role> implements AsyncClientResult<Role> {
  private String name;

  public RoleGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public RoleGetter withName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public Future<Result<Role>> run(FutureCallback<Result<Role>> callback) {
    return sendGetRequest("/authz/roles/" + this.name, callback, new ResponseParser<Role>() {
      @Override
      public Result<Role> parse(HttpResponse response, String body, ContentType contentType) {
        Response<WeaviateRole> resp = this.serializer.toResponse(response.getCode(), body, WeaviateRole.class);
        Role role = Optional.ofNullable(resp.getBody()).map(WeaviateRole::toRole).orElse(null);
        return new Result<>(resp.getStatusCode(), role, resp.getErrors());
      }
    });
  }
}
