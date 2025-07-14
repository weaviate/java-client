package io.weaviate.client.v1.async.cluster.api;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.cluster.model.ReplicationType;
import lombok.Getter;

public class Replicator extends AsyncBaseClient<String> implements AsyncClientResult<String> {
  private String className;
  private String shard;
  private String sourceNode;
  private String targetNode;
  private ReplicationType replicationType;

  public Replicator(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
  }

  public Replicator withClassName(String className) {
    this.className = className;
    return this;
  }

  public Replicator withShard(String shard) {
    this.shard = shard;
    return this;
  }

  public Replicator withSourceNode(String sourceNode) {
    this.sourceNode = sourceNode;
    return this;
  }

  public Replicator withTargetNode(String targetNode) {
    this.targetNode = targetNode;
    return this;
  }

  public Replicator withReplicationType(ReplicationType replicationType) {
    this.replicationType = replicationType;
    return this;
  }

  class RequestBody {
    @SerializedName("collection")
    String className = Replicator.this.className;
    @SerializedName("shard")
    String shard = Replicator.this.shard;
    @SerializedName("sourceNode")
    String sourceNode = Replicator.this.sourceNode;
    @SerializedName("targetNode")
    String targetNode = Replicator.this.targetNode;
    @SerializedName("type")
    ReplicationType replicationType = Replicator.this.replicationType;
  }

  @Getter
  static class ResponseBody {
    @SerializedName("id")
    String replicationId;
  }

  @Override
  public Future<Result<String>> run(FutureCallback<Result<String>> callback) {
    return sendPostRequest("/replication/replicate", new RequestBody(),
        callback, Result.mapParser(ResponseBody.class, ResponseBody::getReplicationId));

  }
}
