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

public class TenantExists extends AsyncBaseClient<Object> implements AsyncClientResult<Boolean> {
  private String className;
  private String tenant;

  public TenantExists(CloseableHttpAsyncClient client, Config config) {
    super(client, config);
  }

  public TenantExists withClassName(String className) {
    this.className = className;
    return this;
  }

  public TenantExists withTenant(String tenant) {
    this.tenant = tenant;
    return this;
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    String path = String.format("/schema/%s/tenants/%s", UrlEncoder.encodePathParam(className), UrlEncoder.encodePathParam(tenant));
    final FutureCallback<Result<Object>> objectCb = callback == null ? null : new FutureCallback<Result<Object>>() {
      @Override
      public void completed(Result<Object> result) {
          callback.completed(result.withNewResult(result.getStatusCode() == HttpStatus.SC_OK));
      }

      @Override
      public void cancelled() {
          callback.cancelled();
      }

      @Override
      public void failed(Exception ex) {
          callback.failed(ex);
      }
    };

    return CompletableFuture.supplyAsync(() -> {
      try {
        Result<Object> result = sendHeadRequest(path, Object.class, objectCb).get();
        return result.withNewResult(result.getStatusCode() == HttpStatus.SC_OK);
      }catch (ExecutionException | InterruptedException e) {
        throw new CompletionException(e);
      }
    });
  }
}