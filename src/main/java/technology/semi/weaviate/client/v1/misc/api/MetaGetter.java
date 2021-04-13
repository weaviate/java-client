package technology.semi.weaviate.client.v1.misc.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.misc.model.Meta;

public class MetaGetter extends BaseClient<Meta> implements ClientResult<Meta> {

  public MetaGetter(Config config) {
    super(config);
  }

  @Override
  public Result<Meta> run() {
    Response<Meta> resp = sendGetRequest("/meta", Meta.class);
    return new Result<>(resp);
  }
}
