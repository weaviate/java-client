package io.weaviate.client6.v1.api.collections.query;

abstract class NearMediaBuilder<SelfT, MediaT>
    extends BaseQueryOptions.Builder<NearMediaBuilder<SelfT, MediaT>, MediaT> {
  // Required query parameters.
  final String media;

  // Optional query parameters.
  Float distance;
  Float certainty;

  public NearMediaBuilder(String media) {
    this.media = media;
  }

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
