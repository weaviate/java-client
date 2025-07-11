package io.weaviate.client.v1.cluster.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.cluster.model.NodesStatusResponse;

public class NodesStatusGetter extends BaseClient<NodesStatusResponse> implements ClientResult<NodesStatusResponse> {
  private String className;
  private Map<String, Object> queryParams = new HashMap<>();

  public NodesStatusGetter(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public NodesStatusGetter withClassName(String className) {
    this.className = className;
    return this;
  }

  public NodesStatusGetter withShard(String shard) {
    this.queryParams.put("shard", shard);
    return this;
  }

  public NodesStatusGetter withOutput(String output) {
    this.queryParams.put("output", output);
    return this;
  }

  @Override
  public Result<NodesStatusResponse> run() {
    return new Result<>(sendGetRequest(path(), NodesStatusResponse.class));
  }

  private String path() {
    String path = "/nodes";

    if (StringUtils.isNotBlank(className)) {
      path += "/" + UrlEncoder.encodePathParam(className);
    }

    List<String> query = new ArrayList<>();
    for (Entry<String, Object> qp : queryParams.entrySet()) {
      query.add(UrlEncoder.encodeQueryParam(qp.getKey(), qp.getValue().toString()));
    }

    if (!query.isEmpty()) {
      path += "?" + String.join("&", query);
    }

    return path;
  }
}
