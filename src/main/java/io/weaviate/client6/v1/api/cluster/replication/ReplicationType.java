package io.weaviate.client6.v1.api.cluster.replication;

import com.google.gson.annotations.SerializedName;

public enum ReplicationType {
  /** A copy of the shard is created on the target node. */
  @SerializedName("COPY")
  COPY,
  /** Shard is moved to the target node and is deleted from the source node. */
  @SerializedName("MOVE")
  MOVE;
}
