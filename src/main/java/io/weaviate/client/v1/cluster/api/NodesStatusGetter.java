package io.weaviate.client.v1.cluster.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.cluster.model.NodesStatusResponse;

public class NodesStatusGetter extends BaseClient<NodesStatusResponse> implements ClientResult<NodesStatusResponse> {

  public NodesStatusGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  @Override
  public Result<NodesStatusResponse> run() {
    Response<NodesStatusResponse> resp = sendGetRequest("/nodes", NodesStatusResponse.class);
    return new Result<>(resp);
  }
}
