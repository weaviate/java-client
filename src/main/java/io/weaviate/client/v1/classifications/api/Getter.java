package io.weaviate.client.v1.classifications.api;

import io.weaviate.client.v1.classifications.model.Classification;
import org.apache.commons.lang3.StringUtils;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class Getter extends BaseClient<Classification> implements ClientResult<Classification> {

  private String id;

  public Getter(HttpClient httpClient, Config config) {
    super(httpClient, config);
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
