package io.weaviate.client.v1.schema.api;

import io.weaviate.client.v1.schema.model.Schema;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

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
