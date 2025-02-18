package io.weaviate.client.v1.rbac.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.rbac.model.Role;

public class RoleAllGetter extends BaseClient<WeaviateRole[]> implements ClientResult<List<Role>> {

  public RoleAllGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  @Override
  public Result<List<Role>> run() {
    Response<WeaviateRole[]> resp = sendGetRequest("/authz/roles", WeaviateRole[].class);
    List<Role> roles = Optional.ofNullable(resp.getBody())
        .map(Arrays::asList)
        .orElse(new ArrayList<>())
        .stream()
        .map(w -> w.toRole())
        .collect(Collectors.toList());
    return new Result<>(resp.getStatusCode(), roles, resp.getErrors());
  }
}
