package io.weaviate.client.v1.async.cluster.api;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.Config;
import io.weaviate.client.base.AsyncBaseClient;
import io.weaviate.client.base.AsyncClientResult;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.util.UrlEncoder;
import io.weaviate.client.v1.auth.provider.AccessTokenProvider;
import io.weaviate.client.v1.cluster.model.ShardingState;
import lombok.Getter;

public class ShardingStateQuerier extends AsyncBaseClient<ShardingState> implements AsyncClientResult<ShardingState> {
  private String className;
  private String shard;

  public ShardingStateQuerier(CloseableHttpAsyncClient httpClient, Config config, AccessTokenProvider tokenProvider) {
    super(httpClient, config, tokenProvider);
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
  public Future<Result<ShardingState>> run(FutureCallback<Result<ShardingState>> callback) {
    String path = "/replication/sharding-state?" + UrlEncoder.encodeQueryParam("collection", className);
    if (shard != null) {
      path += "&" + UrlEncoder.encodeQueryParam("shard", shard);
    }
    return sendGetRequest(path, callback, Result.mapParser(ResponseBody.class, ResponseBody::getState));
  }
}
