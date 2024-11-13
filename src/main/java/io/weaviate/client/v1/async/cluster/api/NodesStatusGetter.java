package io.weaviate.client.v1.async.cluster.api;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.cluster.model.NodesStatusResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.util.concurrent.Future;

public class NodesStatusGetter extends AsyncBaseClient<NodesStatusResponse>
  implements AsyncClientResult<NodesStatusResponse> {

  private String className;
  private String output;

  public NodesStatusGetter(CloseableHttpAsyncClient client, Config config) {
    super(client, config);
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
  public Future<Result<NodesStatusResponse>> run(FutureCallback<Result<NodesStatusResponse>> callback) {
    return sendGetRequest(path(), NodesStatusResponse.class, callback);
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
