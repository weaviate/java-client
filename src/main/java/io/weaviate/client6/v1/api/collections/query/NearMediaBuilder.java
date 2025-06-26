package io.weaviate.client6.v1.api.collections.query;

abstract class NearMediaBuilder<SelfT extends NearMediaBuilder<SelfT, MediaT>, MediaT extends Object>
    extends BaseVectorSearchBuilder<SelfT, MediaT> {
  // Required query parameters.
  final String media;

  public NearMediaBuilder(String media) {
    this.media = media;
  }
}
