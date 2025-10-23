package io.weaviate.client6.v1.api.cluster;

import com.google.gson.annotations.SerializedName;

public record ListShardsResponse(@SerializedName("shardingState") ShardingState shardingState) {
}
