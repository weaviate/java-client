package technology.semi.weaviate.client.v1.data.api;

import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.base.Response;

public class ObjectDeleter extends BaseClient<String> implements Client<Boolean> {

  private String id;

  public ObjectDeleter(Config config) {
    super(config);
  }

  public ObjectDeleter withID(String id) {
    this.id = id;
    return this;
  }

  @Override
  public Boolean run() {
    if (StringUtils.isEmpty(this.id)) {
      return false;
    }
    String path = String.format("/objects/%s", this.id);
    Response<String> resp = sendDeleteRequest(path, String.class);
    return resp.getStatusCode() == 204;
  }
}