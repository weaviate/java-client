package io.weaviate.client.v1.async.cluster.api.replication.api;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.cluster.api.replication.model.ReplicateOperation;

public class ReplicationAllGetter extends AsyncBaseClient<List<ReplicateOperation>>
    implements AsyncClientResult<List<ReplicateOperation>> {

  public ReplicationAllGetter(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  @Override
  public Future<Result<List<ReplicateOperation>>> run(FutureCallback<Result<List<ReplicateOperation>>> callback) {
    return sendGetRequest("/replication/replicate/list?includeHistory=true", callback,
        Result.toListParser(ReplicateOperation[].class));
  }
}
