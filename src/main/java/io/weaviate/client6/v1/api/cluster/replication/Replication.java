package io.weaviate.client6.v1.api.cluster.replication;

import java.util.List;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

public record Replication(
    /** Operation UUID. */
    @SerializedName("id") UUID uuid,
    @SerializedName("collection") String collection,
    @SerializedName("shard") String shard,
    @SerializedName("sourceNode") String sourceNode,
    @SerializedName("targetNode") String targetNode,
    @SerializedName("type") ReplicationType type,
    @SerializedName("status") ReplicationStatus status,
    /** Absent if {@code includeHistory} not enabled. */
    @SerializedName("statusHistory") List<ReplicationStatus> history) {
}
