package technology.semi.weaviate.client.v1.cluster.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.v1.cluster.model.NodesStatusResponse;

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
