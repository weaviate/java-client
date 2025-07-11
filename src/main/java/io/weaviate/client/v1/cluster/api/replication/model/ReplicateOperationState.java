package io.weaviate.client.v1.cluster.api.replication.model;

import com.google.gson.annotations.SerializedName;

public enum ReplicateOperationState {
  @SerializedName("REGISTERED")
  REGISTERED,
  @SerializedName("HYDRATING")
  HYDRATING,
  @SerializedName("FINALIZING")
  FINALIZING,
  @SerializedName("DEHYDRATING")
  DEHYDRATING,
  @SerializedName("READY")
  READY,
  @SerializedName("CANCELLED")
  CANCELLED;
}
