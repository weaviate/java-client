package io.weaviate.client.v1.cluster.api.replication.api;

import java.util.Arrays;
import java.util.List;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
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
    String path = "/replication/replicate/list?includeHistory=true";
    Response<ReplicateOperation[]> resp = sendGetRequest(path, ReplicateOperation[].class);
    return new Result<>(resp, Arrays.asList(resp.getBody()));
  }
}
