package io.weaviate.client.v1.cluster.api.replication.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class ReplicationDeleter extends BaseClient<Void> implements ClientResult<Boolean> {
  private String uuid;

  public ReplicationDeleter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public ReplicationDeleter withUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    return Result
        .voidToBoolean(sendDeleteRequest("/replication/replicate/" + uuid, null, Void.class));
  }
}
