package io.weaviate.client6.v1.api.collections.query;

import io.weaviate.client6.v1.api.collections.vectorindex.Distance;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

/**
 * Metadata is the common base for all properties that are requestes as
 * "_additional". It is an inteface all metadata properties MUST implement to be
 * used in {@link BaseQueryOptions}.
 */
public interface Metadata {
  void appendTo(WeaviateProtoSearchGet.MetadataRequest.Builder metadata);

  /** Include associated vector in the metadata response. */
  public static final Metadata VECTOR = MetadataField.VECTOR;
  /** Include object creation time in the metadata response. */
  public static final Metadata CREATION_TIME_UNIX = MetadataField.CREATION_TIME_UNIX;
  /** Include last update time in the metadata response. */
  public static final Metadata LAST_UPDATE_TIME_UNIX = MetadataField.LAST_UPDATE_TIME_UNIX;
  /**
   * Include raw distance determined as part of the vector search.
   * The units will correspond to the distance metric configured for the vector
   * index; by default {@link Distance#COSINE}.
   *
   * <p>
   * Distance is only applicable to <strong>vector search results</strong>,
   * i.e. all {@code Near-} queries. Hybrid search will not return a distance,
   * as the BM25-VectorSearch fusion algorithm transforms the distance metric.
   *
   * @see <a href=
   *      "https://forum.weaviate.io/t/issue-with-distance-value-in-weaviate-hybrid-search-and-applying-similarity-score-threshold/20443/3">
   *      Distance metric in Hybrid search</a>
   */
  public static final Metadata DISTANCE = MetadataField.DISTANCE;
  /**
   * Include certainty in the metadata response.
   *
   * <p>
   * Certainty is an <i>opinionated</i> measure that always returns a number
   * between 0 and 1. It is therefore usable with fixed-range distance metrics,
   * such as {@code cosine}.
   *
   * @see <a href=
   *      "https://docs.weaviate.io/weaviate/config-refs/distances#distance-vs-certainty">
   *      Distance vs. Certainty</a>
   */
  public static final Metadata CERTAINTY = MetadataField.CERTAINTY;
  /**
   * Include {@code BM25F} score of the search result in the metadata response.
   *
   * <p>
   * {@link Metadata#SCORE} and {@link Metadata#EXPLAIN_SCORE} are only relevant
   * for Hybrid and BM25 search.
   */
  public static final Metadata SCORE = MetadataField.SCORE;
  /**
   * Include the result score broken down into components.
   * The output is an unstructured string that is mostly useful for debugging
   * search results.
   *
   * <p>
   * {@link Metadata#SCORE} and {@link Metadata#EXPLAIN_SCORE} are only relevant
   * for Hybrid and BM25 search.
   */
  public static final Metadata EXPLAIN_SCORE = MetadataField.EXPLAIN_SCORE;

  /**
   * MetadataField are collection properties that can be requested for any object.
   */
  enum MetadataField implements Metadata {
    UUID,
    VECTOR,
    CREATION_TIME_UNIX,
    LAST_UPDATE_TIME_UNIX,
    DISTANCE,
    CERTAINTY,
    SCORE,
    EXPLAIN_SCORE;

    public void appendTo(WeaviateProtoSearchGet.MetadataRequest.Builder metadata) {
      switch (this) {
        case UUID:
          metadata.setUuid(true);
          break;
        case VECTOR:
          metadata.setVector(true);
          break;
        case CREATION_TIME_UNIX:
          metadata.setCreationTimeUnix(true);
          break;
        case LAST_UPDATE_TIME_UNIX:
          metadata.setLastUpdateTimeUnix(true);
          break;
        case DISTANCE:
          metadata.setDistance(true);
          break;
        case CERTAINTY:
          metadata.setCertainty(true);
          break;
        case EXPLAIN_SCORE:
          metadata.setExplainScore(true);
          break;
        case SCORE:
          metadata.setScore(true);
          break;
      }
    }
  }
}
