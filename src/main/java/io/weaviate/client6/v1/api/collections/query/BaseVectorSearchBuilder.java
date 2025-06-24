package io.weaviate.client6.v1.api.collections.query;

abstract class BaseVectorSearchBuilder<SelfT extends BaseVectorSearchBuilder<SelfT, NearT>, NearT extends Object>
    extends BaseQueryOptions.Builder<SelfT, NearT> {

  // Optional query parameters.
  Float distance;
  Float certainty;

  @SuppressWarnings("unchecked")
  public SelfT distance(float distance) {
    this.distance = distance;
    return (SelfT) this;
  }

  @SuppressWarnings("unchecked")
  public SelfT certainty(float certainty) {
    this.certainty = certainty;
    return (SelfT) this;
  }
}
