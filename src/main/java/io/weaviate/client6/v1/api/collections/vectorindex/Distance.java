package io.weaviate.client6.v1.api.collections.vectorindex;

import com.google.gson.annotations.SerializedName;

public enum Distance {
  @SerializedName("cosine")
  COSINE,
  @SerializedName("dot")
  DOT,
  @SerializedName("l2-squared")
  L2_SQUARED,
  @SerializedName("hamming")
  HAMMING,
  @SerializedName("manhattan")
  MANHATTAN;
}
