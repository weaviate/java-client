package io.weaviate.client6.v1.api.cluster;

import com.google.gson.annotations.SerializedName;

public enum NodeVerbosity {
  @SerializedName("minimal")
  MINIMAL,
  @SerializedName("verbose")
  VERBOSE;
}
