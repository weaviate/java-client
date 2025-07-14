package io.weaviate.client.v1.async.cluster.api.replication.api;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;

public class ReplicationCanceler extends AsyncBaseClient<Boolean> implements AsyncClientResult<Boolean> {
  private String uuid;

  public ReplicationCanceler(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public ReplicationCanceler withUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  @Override
  public Future<Result<Boolean>> run(FutureCallback<Result<Boolean>> callback) {
    return sendPostRequest("/replication/replicate/" + uuid + "/cancel", null,
        callback, Result.voidToBooleanParser());
  }
}
