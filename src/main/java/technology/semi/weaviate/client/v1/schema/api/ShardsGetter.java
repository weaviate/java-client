package technology.semi.weaviate.client.v1.schema.api;

import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.base.WeaviateErrorMessage;
import technology.semi.weaviate.client.base.WeaviateErrorResponse;
import technology.semi.weaviate.client.v1.schema.model.Shard;

public class ShardsGetter extends BaseClient<Shard[]> implements ClientResult<Shard[]> {
  private String className;

  public ShardsGetter(Config config) {
    super(config);
  }

  public ShardsGetter withClassName(String className) {
    this.className = className;
    return this;
  }

  @Override
  public Result<Shard[]> run() {
    if (StringUtils.isEmpty(this.className)) {
      WeaviateErrorMessage errorMessage = WeaviateErrorMessage.builder()
        .message("className cannot be empty").build();
      WeaviateErrorResponse errors = WeaviateErrorResponse.builder()
        .error(Collections.singletonList(errorMessage)).build();
      return new Result<>(HttpStatus.SC_BAD_REQUEST, null, errors);
    }
    String path = String.format("/schema/%s/shards", this.className);
    Response<Shard[]> resp = sendGetRequest(path, Shard[].class);
    return new Result<>(resp);
  }
}
