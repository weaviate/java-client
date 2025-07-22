package io.weaviate.client.v1.async.cluster.api.replication.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.cluster.api.replication.model.ReplicateOperation;

public class ReplicationQuerier extends AsyncBaseClient<List<ReplicateOperation>>
    implements AsyncClientResult<List<ReplicateOperation>> {
  private Map<String, Object> queryParams = new HashMap<>();

  public ReplicationQuerier(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
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
  public Future<Result<List<ReplicateOperation>>> run(FutureCallback<Result<List<ReplicateOperation>>> callback) {
    String path = "/replication/replicate/list";

    List<String> query = new ArrayList<>();
    for (Entry<String, Object> qp : queryParams.entrySet()) {
      query.add(UrlEncoder.encodeQueryParam(qp.getKey(), qp.getValue().toString()));
    }

    if (!query.isEmpty()) {
      path += "?" + String.join("&", query);
    }
    return sendGetRequest(path, callback, Result.toListParser(ReplicateOperation[].class));
  }
}
