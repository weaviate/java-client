package io.weaviate.client6.v1.api.cluster;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public record ShardReplica(
    @SerializedName("shard") String shardName,
    @SerializedName("replicas") List<String> replicas) {
}
