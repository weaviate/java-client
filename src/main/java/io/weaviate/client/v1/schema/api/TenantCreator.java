package io.weaviate.client.v1.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.schema.model.Tenant;
import org.apache.http.HttpStatus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TenantCreator extends BaseClient<Tenant[]> implements ClientResult<Boolean> {

  private String className;
  private Tenant[] tenants;

  public TenantCreator(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public TenantCreator withClassName(String className) {
    this.className = className;
    return this;
  }

  public TenantCreator withTenants(Tenant... tenants) {
    this.tenants = tenants;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    String path = String.format("/schema/%s/tenants", encode(className));
    Response<Tenant[]> resp = sendPostRequest(path, tenants, Tenant[].class);
    return new Result<>(resp.getStatusCode(), resp.getStatusCode() == HttpStatus.SC_OK, resp.getErrors());
  }

  private String encode(String value) {
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      return null;
    }
  }
}
