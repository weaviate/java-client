package technology.semi.weaviate.client.v1.schema.api;

import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.v1.schema.api.model.Schema;

public class SchemaGetter extends BaseClient<Schema> implements Client<Schema> {

  public SchemaGetter(Config config) {
    super(config);
  }

  @Override
  public Schema run() {
    Response<Schema> resp = sendGetRequest("/schema", Schema.class);
    return resp.getBody();
  }
}
