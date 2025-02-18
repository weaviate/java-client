package io.weaviate.client.v1.async.rbac.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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

public class RoleAllGetter extends AsyncBaseClient<List<Role>> implements AsyncClientResult<List<Role>> {

  public RoleAllGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  @Override
  public Future<Result<List<Role>>> run(FutureCallback<Result<List<Role>>> callback) {
    return sendGetRequest("/authz/roles", callback, new ResponseParser<List<Role>>() {
      @Override
      public Result<List<Role>> parse(HttpResponse response, String body, ContentType contentType) {
        Response<WeaviateRole[]> resp = this.serializer.toResponse(response.getCode(), body, WeaviateRole[].class);
        List<Role> roles = Optional.ofNullable(resp.getBody())
            .map(Arrays::asList)
            .orElse(new ArrayList<>())
            .stream()
            .map(w -> w.toRole())
            .collect(Collectors.toList());
        return new Result<>(resp.getStatusCode(), roles, resp.getErrors());
      }
    });
  }
}
