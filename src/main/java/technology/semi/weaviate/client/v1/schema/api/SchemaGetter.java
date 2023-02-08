package technology.semi.weaviate.client.v1.schema.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.base.http.HttpClient;
import technology.semi.weaviate.client.v1.schema.model.Schema;

public class SchemaGetter extends BaseClient<Schema> implements ClientResult<Schema> {

  public SchemaGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  @Override
  public Result<Schema> run() {
    Response<Schema> resp = sendGetRequest("/schema", Schema.class);
    return new Result<>(resp);
  }
}
