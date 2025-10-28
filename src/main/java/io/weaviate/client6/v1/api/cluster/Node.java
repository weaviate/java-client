package io.weaviate.client6.v1.api.cluster;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public record Node(
    @SerializedName("name") String name,
    @SerializedName("status") NodeStatus status,
    /** Commit hash of the Weaviate build the node is running. */
    @SerializedName("gitHash") String gitHash,
    /** Weaviate version the node is running. */
    @SerializedName("version") String version,
    /**
     * Can be {@code null} if "minimal" output is requested.
     *
     * @see NodeVerbosity#MINIMAL.
     */
    @SerializedName("stats") CollectionStats stats,
    /**
     * Can be {@code null} if "minimal" output is requested.
     *
     * @see NodeVerbosity#MINIMAL.
     */
    @SerializedName("shards") List<Shard> shards) {
}
