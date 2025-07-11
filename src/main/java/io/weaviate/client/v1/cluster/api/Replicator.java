package io.weaviate.client.v1.cluster.api;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.cluster.api.Replicator.ResponseBody;
import io.weaviate.client.v1.cluster.model.ReplicationType;

public class Replicator extends BaseClient<ResponseBody> implements ClientResult<String> {
  private String className;
  private String shard;
  private String sourceNode;
  private String targetNode;
  private ReplicationType replicationType;

  public Replicator(HttpClient httpClient, Config config) {
    super(httpClient, config);
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

  static class ResponseBody {
    @SerializedName("id")
    String replicationId;
  }

  @Override
  public Result<String> run() {
    Response<ResponseBody> resp = sendPostRequest("/replication/replicate", new RequestBody(), ResponseBody.class);
    return new Result<>(resp, resp.getBody().replicationId);
  }
}
