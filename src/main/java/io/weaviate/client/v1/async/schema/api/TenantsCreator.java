package io.weaviate.client.v1.async.schema.api;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.async.ResponseParser;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.schema.model.Tenant;

public class TenantsCreator extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
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
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    String path = String.format("/schema/%s/tenants", UrlEncoder.encodePathParam(className));

    return sendPostRequest(path, tenants, callback, new ResponseParser<Boolean>() {
      @Override
      public Result<Boolean> parse(HttpResponse response, String body, ContentType contentType) {
          Response<Tenant[]> resp = serializer.toResponse(response.getCode(), body, Tenant[].class);
          return new Result<>(resp.getStatusCode(), resp.getStatusCode() == HttpStatus.SC_OK, resp.getErrors());
      }
    });
  }
}
