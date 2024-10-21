package io.weaviate.client.v1.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.UrlEncoder;
import org.apache.hc.core5.http.HttpStatus;

public class TenantsDeleter extends BaseClient<Object> implements ClientResult<Boolean> {

  private String className;
  private String[] tenants;

  public TenantsDeleter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public TenantsDeleter withClassName(String className) {
    this.className = className;
    return this;
  }

  public TenantsDeleter withTenants(String... tenants) {
    this.tenants = tenants;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    String path = String.format("/schema/%s/tenants", UrlEncoder.encodePathParam(className));
    Response<Object> resp = sendDeleteRequest(path, tenants, Object.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == HttpStatus.SC_OK, resp.getErrors());
  }
}
