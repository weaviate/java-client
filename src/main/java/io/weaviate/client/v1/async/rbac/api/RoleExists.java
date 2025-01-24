package io.weaviate.client.v1.async.rbac.api;

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
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.rbac.model.Role;

public class RoleExists extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private final RoleGetter getter;

  public RoleExists(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
    this.getter = new RoleGetter(httpClient, config, tokenProvider);
  }

  public RoleExists withName(String name) {
    this.getter.withName(name);
    return this;
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        Result<Role> resp = this.getter.run().get();
        if (resp.hasErrors()) {
          WeaviateError error = resp.getError();
          return new Result<>(error.getStatusCode(), null,
              WeaviateErrorResponse.builder().error(error.getMessages()).build());
        }
        return new Result<Boolean>(HttpStatus.SC_OK, resp.getResult() != null, null);
      } catch (InterruptedException | ExecutionException e) {
        throw new CompletionException(e);
      }
    });
  }
}
