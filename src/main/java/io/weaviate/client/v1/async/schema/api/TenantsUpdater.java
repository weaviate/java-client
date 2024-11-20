package io.weaviate.client.v1.async.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.async.ResponseParser;
import io.weaviate.client.base.util.DbVersionSupport;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.schema.model.Tenant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;

public class TenantsUpdater extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {

  private final static int BATCH_SIZE = 100;
  private final DbVersionSupport dbVersionSupport;
  private String className;
  private Tenant[] tenants;

  public TenantsUpdater(CloseableHttpAsyncClient client, Config config, DbVersionSupport dbVersionSupport) {
    super(client, config);
    this.dbVersionSupport = dbVersionSupport;
  }

  public TenantsUpdater withClassName(String className) {
    this.className = className;
    return this;
  }

  public TenantsUpdater withTenants(Tenant... tenants) {
    this.tenants = tenants;
    return this;
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    if (dbVersionSupport.supportsOnly100TenantsInOneRequest() && tenants != null && tenants.length > BATCH_SIZE) {
      CompletableFuture<Result<Boolean>> updateALl = CompletableFuture.supplyAsync(() -> chunkTenants(tenants, BATCH_SIZE)).thenApplyAsync(tenants -> {
        for (List<Tenant> batch : tenants) {
          try {
            Result<Boolean> resp = updateTenants(batch.toArray(new Tenant[0]), null).get();
            if (resp.hasErrors()) {
              return resp;
            }
          } catch (InterruptedException | ExecutionException e) {
            throw new CompletionException(e);
          }
        }
        return new Result<>(200, true, null);
      });
      if (callback != null) {
        return updateALl.whenComplete((booleanResult, e) -> {
          callback.completed(booleanResult);
          if (e != null) {
            callback.failed(new Exception(e));
          }
        });
      }
      return updateALl;
    }
    return updateTenants(tenants, callback);
  }

  private Future<Result<Boolean>> updateTenants(Tenant[] tenants, FutureCallback<Result<Boolean>> callback) {
    String path = String.format("/schema/%s/tenants", UrlEncoder.encodePathParam(className));
    return sendPutRequest(path, tenants, callback, new ResponseParser<Boolean>() {
      @Override
      public Result<Boolean> parse(HttpResponse response, String body, ContentType contentType) {
        Response<Tenant[]> resp = serializer.toResponse(response.getCode(), body, Tenant[].class);
        return new Result<>(resp.getStatusCode(), resp.getStatusCode() == HttpStatus.SC_OK, resp.getErrors());
      }
    });
  }

  private Collection<List<Tenant>> chunkTenants(Tenant[] tenants, int chunkSize) {
    AtomicInteger counter = new AtomicInteger();
    return Stream.of(tenants).collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize)).values();
  }
}
