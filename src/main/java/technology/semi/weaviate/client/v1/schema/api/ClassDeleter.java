package technology.semi.weaviate.client.v1.schema.api;

import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.Client;
import technology.semi.weaviate.client.base.Response;

public class ClassDeleter extends BaseClient<String> implements Client<Boolean> {

  private String className;

  public ClassDeleter(Config config) {
    super(config);
  }

  public ClassDeleter withClassName(String className) {
    this.className = className;
    return this;
  }

  @Override
  public Boolean run() {
    if (StringUtils.isEmpty(this.className)) {
      return false;
    }
    String path = String.format("/schema/%s", this.className);
    Response<String> resp = sendDeleteRequest(path, String.class);
    return resp.getStatusCode() == 200;
  }
}