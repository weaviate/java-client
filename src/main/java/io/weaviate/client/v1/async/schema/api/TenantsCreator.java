package io.weaviate.client.v1.async.schema.api;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.schema.model.Tenant;

public class TenantsCreator extends AsyncBaseClient<Tenant[]> implements AsyncClientResult<Boolean> {
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

    final FutureCallback<Result<Tenant[]>> tennantCb = callback == null ? null : new FutureCallback<Result<Tenant[]>>() {
      @Override
      public void completed(Result<Tenant[]> tenants) {
        callback.completed(tenants.<Boolean> withNewResult(tenants.getStatusCode() == HttpStatus.SC_OK));
      }

      @Override
      public void failed(Exception ex) {
          callback.failed(ex);
      }

      @Override
      public void cancelled() {
          callback.cancelled();
      }
    };

    return CompletableFuture.supplyAsync(() -> {
      try {
        Result<Tenant[]> result = sendPostRequest(path, tenants, Tenant[].class, tennantCb).get();
        return result.withNewResult(result.getStatusCode() == HttpStatus.SC_OK);
      } catch (ExecutionException | InterruptedException e) {
        throw new CompletionException(e);
      }
    });
  }
}
