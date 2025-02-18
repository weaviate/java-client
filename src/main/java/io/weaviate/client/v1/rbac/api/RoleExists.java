package io.weaviate.client.v1.rbac.api;

import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.rbac.model.Role;

public class RoleExists extends BaseClient<Boolean> implements ClientResult<Boolean> {
  private RoleGetter getter;

  public RoleExists(HttpClient httpClient, Config config) {
    super(httpClient, config);
    this.getter = new RoleGetter(httpClient, config);
  }

  public RoleExists withName(String name) {
    this.getter.withName(name);
    return this;
  }

  @Override
  public Result<Boolean> run() {
    Result<Role> resp = this.getter.run();
    if (resp.hasErrors()) {
      WeaviateError error = resp.getError();
      return new Result<>(error.getStatusCode(), null,
          WeaviateErrorResponse.builder().error(error.getMessages()).build());

    }
    return new Result<Boolean>(HttpStatus.SC_OK, resp.getResult() != null, null);
  }
}
