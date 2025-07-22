package io.weaviate.client.v1.cluster.api.replication.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.cluster.api.replication.model.ReplicateOperation;

public class ReplicationQuerier extends BaseClient<ReplicateOperation[]>
    implements ClientResult<List<ReplicateOperation>> {
  private Map<String, Object> queryParams = new HashMap<>();

  public ReplicationQuerier(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public ReplicationQuerier withClassName(String className) {
    this.queryParams.put("collection", className);
    return this;
  }

  public ReplicationQuerier withShard(String shard) {
    this.queryParams.put("shard", shard);
    return this;
  }

  public ReplicationQuerier withTargetNode(String targetNode) {
    this.queryParams.put("targetNode", targetNode);
    return this;
  }

  public ReplicationQuerier withIncludeHistory(boolean includeHistory) {
    this.queryParams.put("includeHistory", includeHistory);
    return this;
  }

  @Override
  public Result<List<ReplicateOperation>> run() {
    String path = "/replication/replicate/list";

    List<String> query = new ArrayList<>();
    for (Entry<String, Object> qp : queryParams.entrySet()) {
      query.add(UrlEncoder.encodeQueryParam(qp.getKey(), qp.getValue().toString()));
    }

    if (!query.isEmpty()) {
      path += "?" + String.join("&", query);
    }

    return Result.toList(sendGetRequest(path, ReplicateOperation[].class));
  }
}
