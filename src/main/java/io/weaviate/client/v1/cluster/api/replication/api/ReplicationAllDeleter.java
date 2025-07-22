package io.weaviate.client.v1.cluster.api.replication.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class ReplicationAllDeleter extends BaseClient<Void> implements ClientResult<Boolean> {

  public ReplicationAllDeleter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  @Override
  public Result<Boolean> run() {
    return Result
        .voidToBoolean(sendDeleteRequest("/replication/replicate", null, Void.class));
  }
}
