package io.weaviate.client.v1.cluster.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.cluster.model.NodesStatusResponse;
import org.apache.commons.lang3.StringUtils;

public class NodesStatusGetter extends BaseClient<NodesStatusResponse> implements ClientResult<NodesStatusResponse> {

  private String className;
  private String output;

  public NodesStatusGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public NodesStatusGetter withClassName(String className) {
    this.className = className;
    return this;
  }

  public NodesStatusGetter withOutput(String output) {
    this.output = output;
    return this;
  }

  @Override
  public Result<NodesStatusResponse> run() {
    Response<NodesStatusResponse> resp = sendGetRequest(path(), NodesStatusResponse.class);
    return new Result<>(resp);
  }

  private String path() {
    String path = "/nodes";
    if (StringUtils.isNotBlank(className)) {
      path = String.format("%s/%s", path, UrlEncoder.encodePathParam(className));
    }
    if (StringUtils.isNotBlank(output)) {
      path = String.format("%s?%s", path, UrlEncoder.encodeQueryParam("output", output));
    }
    return path;
  }
}
