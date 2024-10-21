package io.weaviate.client.v1.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.UrlEncoder;
import org.apache.hc.core5.http.HttpStatus;

public class TenantsExists extends BaseClient<Object> implements ClientResult<Boolean> {

  private String className;
  private String tenant;

  public TenantsExists(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public TenantsExists withClassName(String className) {
    this.className = className;
    return this;
  }

  public TenantsExists withTenant(String tenant) {
    this.tenant = tenant;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    String path = String.format("/schema/%s/tenants/%s", UrlEncoder.encodePathParam(className), UrlEncoder.encodePathParam(tenant));
    Response<Object> resp = sendHeadRequest(path, Object.class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == HttpStatus.SC_OK, resp.getErrors());
  }
}
