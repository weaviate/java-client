package io.weaviate.client6.v1.api.cluster.replication;

import com.google.gson.annotations.SerializedName;

public enum ReplicationState {
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
  CANCELED,

}
