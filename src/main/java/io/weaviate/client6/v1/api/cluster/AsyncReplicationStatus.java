package io.weaviate.client6.v1.api.cluster;

import com.google.gson.annotations.SerializedName;

public record AsyncReplicationStatus(
    @SerializedName("objectsPropagated") long objectsPropagated,
    @SerializedName("startDiffTimeUnixMillis") long startDiffTimeUnixMillis,
    @SerializedName("targetNode") String targetNode) {
}
