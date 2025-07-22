package io.weaviate.client.v1.cluster.api.replication.api;

import java.util.List;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.cluster.api.replication.model.ReplicateOperation;

public class ReplicationAllGetter extends BaseClient<ReplicateOperation[]>
    implements ClientResult<List<ReplicateOperation>> {

  public ReplicationAllGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  @Override
  public Result<List<ReplicateOperation>> run() {
    return Result.toList(sendGetRequest("/replication/replicate/list?includeHistory=true", ReplicateOperation[].class));
  }
}
