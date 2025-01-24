package io.weaviate.client.v1.async.rbac.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

public class AssignedUsersGetter extends AsyncBaseClient<List<String>> implements AsyncClientResult<List<String>> {
  private String role;

  public AssignedUsersGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public AssignedUsersGetter withRole(String role) {
    this.role = role;
    return this;
  }

  @Override
  public Future<Result<List<String>>> run(FutureCallback<Result<List<String>>> callback) {
    return sendGetRequest(path(), callback, new ResponseParser<List<String>>() {
      @Override
      public Result<List<String>> parse(HttpResponse response, String body, ContentType contentType) {
        Response<String[]> resp = this.serializer.toResponse(response.getCode(), body, String[].class);
        List<String> roles = Optional.ofNullable(resp.getBody())
            .map(Arrays::asList)
            .orElse(new ArrayList<>());
        return new Result<>(resp.getStatusCode(), roles, resp.getErrors());
      }
    });
  }

  private String path() {
    return String.format("/authz/roles/%s/users", this.role);
  }
}
