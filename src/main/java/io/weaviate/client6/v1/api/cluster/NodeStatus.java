package io.weaviate.client6.v1.api.cluster;

import com.google.gson.annotations.SerializedName;

public enum NodeStatus {
  /** The node is functional and operating normally. */
  @SerializedName("HEALTHY")
  HEALTHY,
  /** The node is down after encountering a problem. */
  @SerializedName("UNHEALTHY")
  UNHEALTHY,
  /** The node is not available. */
  @SerializedName("UNAVAILABLE")
  UNAVAILABLE,
  /** Liveness probe to a node timed out. */
  @SerializedName("TIMEOUT")
  TIMEOUT;
};
