package io.weaviate.client6.v1.api.cluster;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public record Node(
    @SerializedName("name") String name,
    @SerializedName("status") String status,
    @SerializedName("gitHash") String gitHash,
    @SerializedName("version") String version,
    @SerializedName("stats") CollectionStats stats,
    @SerializedName("shards") List<Shard> shards) {
}
