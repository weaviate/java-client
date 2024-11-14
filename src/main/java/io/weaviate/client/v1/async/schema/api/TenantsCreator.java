package io.weaviate.client.v1.async.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.schema.model.Tenant;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

public class TenantsCreator extends AsyncBaseClient<Tenant[]> implements AsyncClientResult<Tenant[]> {
  private String className;
  private Tenant[] tenants;

  public TenantsCreator(CloseableHttpAsyncClient client, Config config) {
    super(client, config);
  }

  public TenantsCreator withClassName(String className) {
    this.className = className;
    return this;
  }

  public TenantsCreator withTenants(Tenant... tenants) {
    this.tenants = tenants;
    return this;
  }

  @Override
  public Future<Result<Tenant[]>> run(FutureCallback<Result<Tenant[]>> callback) {
    String path = String.format("/schema/%s/tenants", UrlEncoder.encodePathParam(className));
    return sendPostRequest(path, tenants, Tenant[].class, callback);
  }
}
