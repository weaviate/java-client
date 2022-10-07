package technology.semi.weaviate.client.v1.misc.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.misc.model.NodesStatusResponse;

public class NodesStatusGetter extends BaseClient<NodesStatusResponse> implements ClientResult<NodesStatusResponse> {

  public NodesStatusGetter(Config config) {
    super(config);
  }

  @Override
  public Result<NodesStatusResponse> run() {
    Response<NodesStatusResponse> resp = sendGetRequest("/nodes", NodesStatusResponse.class);
    return new Result<>(resp);
  }
}
