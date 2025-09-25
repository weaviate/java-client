package io.weaviate.client6.v1.api.collections.query;

abstract class BaseVectorSearchBuilder<SelfT extends BaseVectorSearchBuilder<SelfT, NearT>, NearT extends Object>
    extends BaseQueryOptions.Builder<SelfT, NearT> {

  // Optional query parameters.
  Float distance;
  Float certainty;

  /**
   * Discard objects whose vectors are further away
   * from the target vector than the threshold.
   *
   * <p>
   * Use {@link Hybrid.Builder#maxVectorDistance(float)} if {@link NearVector} or
   * {@link NearText} are used as a vector search component in hybrid search.
   */
  @SuppressWarnings("unchecked")
  public SelfT distance(float distance) {
    this.distance = distance;
    return (SelfT) this;
  }

  /**
   * Discard objects whose vectors are further away
   * from the target vector than the threshold according
   * to a normalized ({@code 0 <= c <= 0}) distance.
   *
   * <p>
   * Certainty is only meaningful for {@code cosine} distance.
   * Prefer using {@link #distance(float)} to limit search results.
   */
  @SuppressWarnings("unchecked")
  public SelfT certainty(float certainty) {
    this.certainty = certainty;
    return (SelfT) this;
  }
}
