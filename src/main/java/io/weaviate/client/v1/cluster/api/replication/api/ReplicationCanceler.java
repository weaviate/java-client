package io.weaviate.client.v1.cluster.api.replication.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class ReplicationCanceler extends BaseClient<Void> implements ClientResult<Boolean> {
  private String uuid;

  public ReplicationCanceler(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public ReplicationCanceler withUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  @Override
  public Result<Boolean> run() {
    return Result
        .voidToBoolean(sendPostRequest("/replication/replicate/" + uuid + "/cancel", null, Void.class));
  }
}
