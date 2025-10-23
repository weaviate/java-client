package io.weaviate.client6.v1.api.cluster;

import com.google.gson.annotations.SerializedName;

public record CollectionStats(
    @SerializedName("shardCount") int shardCount,
    @SerializedName("objectCount") long objectCount) {
}
