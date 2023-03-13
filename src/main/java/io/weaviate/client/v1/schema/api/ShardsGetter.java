package io.weaviate.client.v1.schema.api;

import io.weaviate.client.v1.schema.model.Shard;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.base.WeaviateErrorResponse;
import io.weaviate.client.base.http.HttpClient;

public class ShardsGetter extends BaseClient<Shard[]> implements ClientResult<Shard[]> {
  private String className;

  public ShardsGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
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
