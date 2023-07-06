package io.weaviate.client.v1.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.schema.model.Tenant;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TenantsGetter extends BaseClient<Tenant[]> implements ClientResult<List<Tenant>> {

  private String className;

  public TenantsGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public TenantsGetter withClassName(String className) {
    this.className = className;
    return this;
  }

  @Override
  public Result<List<Tenant>> run() {
    String path = String.format("/schema/%s/tenants", UrlEncoder.encodePathParam(className));
    Response<Tenant[]> resp = sendGetRequest(path, Tenant[].class);

    List<Tenant> tenants = Optional.ofNullable(resp.getBody())
      .map(Arrays::asList)
      .orElse(null);
    return new Result<>(resp.getStatusCode(), tenants, resp.getErrors());
  }
}
