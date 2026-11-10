package io.weaviate.client6.v1.api.cluster;

import com.google.gson.annotations.SerializedName;

public record Shard(
    @SerializedName("name") String name,
    @SerializedName("class") String collection,
    @SerializedName("objectCount") int objectCount,
    @SerializedName("vectorIndexingStatus") VectorIndexingStatus vectorIndexingStatus,
    @SerializedName("vectorQueueLength") int vectorQueueLength,
    @SerializedName("compressed") boolean compressed,
    @SerializedName("loaded") boolean loaded,
    @SerializedName("numberOfReplicas") int numberOfReplicas,
    @SerializedName("replicationFactor") int replicationFactor) {
}
