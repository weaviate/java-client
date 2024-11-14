package io.weaviate.client.v1.async.schema.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.v1.schema.model.Shard;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpStatus;

public class ShardsGetter extends AsyncBaseClient<Shard[]> implements AsyncClientResult<Shard[]> {
  private String className;

  public ShardsGetter(CloseableHttpAsyncClient client, Config config) {
    super(client, config);
  }

  public ShardsGetter withClassName(String className) {
    this.className = className;
    return this;
  }

  @Override
  public Future<Result<Shard[]>> run(FutureCallback<Result<Shard[]>> callback) {
    if (StringUtils.isEmpty(this.className)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
        .message("className cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
        .error(Collections.singletonList(errorMessage)).build();
      return CompletableFuture.completedFuture(new Result<>(HttpStatus.SC_BAD_REQUEST, null, errors));
    }
    return sendGetRequest(String.format("/schema/%s/shards", this.className), Shard[].class, callback);
  }
}
