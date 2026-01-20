package io.weaviate.client6.v1.api.collections.vectorindex;

import com.google.gson.annotations.SerializedName;

/**
 * Distance metrics supported for vector search.
 *
 * @see <a href=
 *      "https://docs.weaviate.io/weaviate/config-refs/distances#available-distance-metrics">Availabe
 *      distance metrics in Weaviate</a>
 */
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
