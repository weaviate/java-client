package io.weaviate.client6.v1.api.collections.config;

import com.google.gson.annotations.SerializedName;

public record Shard(
    @SerializedName("name") String name,
    @SerializedName("status") String status,
    @SerializedName("vectorQueueSize") long vectorQueueSize) {
}
