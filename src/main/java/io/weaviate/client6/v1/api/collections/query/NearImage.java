package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;

public record NearImage(String image, Float distance, Float certainty, BaseQueryOptions common)
    implements SearchOperator {

  public static NearImage of(String image) {
    return of(image, ObjectBuilder.identity());
  }

  public static NearImage of(String image, Function<Builder, ObjectBuilder<NearImage>> fn) {
    return fn.apply(new Builder(image)).build();
  }

  public NearImage(Builder builder) {
    this(
        builder.image,
        builder.distance,
        builder.certainty,
        builder.baseOptions());
  }

  public static class Builder extends BaseQueryOptions.Builder<Builder, NearImage> {
    // Required query parameters.
    private final String image;

    // Optional query parameters.
    private Float distance;
    private Float certainty;

    public Builder(String image) {
      this.image = image;
    }

    public Builder distance(float distance) {
      this.distance = distance;
      return this;
    }

    public Builder certainty(float certainty) {
      this.certainty = certainty;
      return this;
    }

    @Override
    public final NearImage build() {
      return new NearImage(this);
    }
  }

  @Override
  public void appendTo(io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);

    var nearImage = WeaviateProtoBaseSearch.NearImageSearch.newBuilder();
    nearImage.setImage(image);

    if (certainty != null) {
      nearImage.setCertainty(certainty);
    } else if (distance != null) {
      nearImage.setDistance(distance);
    }

    req.setNearImage(nearImage);
  }
}
