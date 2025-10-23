package io.weaviate.client6.v1.api.cluster;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public record ShardingState(
    @SerializedName("collection") String collection,
    @SerializedName("shards") List<ShardReplica> shards) {
}
