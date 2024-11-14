package io.weaviate.client.v1.async.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.async.ResponseParser;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.schema.model.Tenant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;

public class TenantsGetter extends AsyncBaseClient<List<Tenant>> implements AsyncClientResult<List<Tenant>> {
  private String className;

  public TenantsGetter(CloseableHttpAsyncClient client, Config config) {
    super(client, config);
  }

  @Override
  public Future<Result<List<Tenant>>> run(FutureCallback<Result<List<Tenant>>> callback) {
    String path = String.format("/schema/%s/tenants", UrlEncoder.encodePathParam(className));
    return sendGetRequest(path, callback, new ResponseParser<List<Tenant>>() {
      @Override
      public Result<List<Tenant>> parse(HttpResponse response, String body, ContentType contentType) {
        Response<Tenant[]> resp = this.serializer.toResponse(response.getCode(), body, Tenant[].class);
        List<Tenant> tenants = Optional.ofNullable(resp.getBody())
          .map(Arrays::asList)
          .orElse(null);
        return new Result<>(resp.getStatusCode(), tenants, resp.getErrors());
      }
    });
  }
}
