package io.weaviate.client.v1.cluster.api;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.cluster.api.ShardingStateQuerier.ResponseBody;
import io.weaviate.client.v1.cluster.model.ShardingState;
import lombok.Getter;

public class ShardingStateQuerier extends BaseClient<ResponseBody> implements ClientResult<ShardingState> {
  private String className;
  private String shard;

  public ShardingStateQuerier(HttpClient httpClient, Config config) {
    super(httpClient, config);
  }

  public ShardingStateQuerier withClassName(String className) {
    this.className = className;
    return this;
  }

  public ShardingStateQuerier withShard(String shard) {
    this.shard = shard;
    return this;
  }

  @Getter
  static class ResponseBody {
    @SerializedName("shardingState")
    ShardingState state;
  }

  @Override
  public Result<ShardingState> run() {
    String path = "/replication/sharding-state?" + UrlEncoder.encodeQueryParam("collection", className);
    if (shard != null) {
      path += "&" + UrlEncoder.encodeQueryParam("shard", shard);
    }
    return Result.map(sendGetRequest(path, ResponseBody.class), ResponseBody::getState);
  }
}
