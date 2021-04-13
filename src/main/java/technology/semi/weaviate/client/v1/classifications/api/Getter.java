package technology.semi.weaviate.client.v1.classifications.api;

import org.apache.commons.lang3.StringUtils;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.classifications.model.Classification;

public class Getter extends BaseClient<Classification> implements ClientResult<Classification> {

  private String id;

  public Getter(Config config) {
    super(config);
  }

  public Getter withID(String id) {
    this.id = id;
    return this;
  }

  @Override
  public Result<Classification> run() {
    if (StringUtils.isBlank(id)) {
      return null;
    }
    String path = String.format("/classifications/%s", id);
    Response<Classification> resp = sendGetRequest(path, Classification.class);
    return new Result<>(resp);
  }
}
