package io.weaviate.client6.v1.api.cluster.replication;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public record ReplicationStatus(
    @SerializedName("state") ReplicationState state,
    @SerializedName("errors") List<String> errors) {
}
