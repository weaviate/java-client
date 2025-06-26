package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearVideo(String video, Float distance, Float certainty, BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static NearVideo of(String video) {
    return of(video, ObjectBuilder.identity());
  }

  public static NearVideo of(String video, Function<Builder, ObjectBuilder<NearVideo>> fn) {
    return fn.apply(new Builder(video)).build();
  }

  public NearVideo(Builder builder) {
    this(
        builder.media,
        builder.distance,
        builder.certainty,
        builder.baseOptions());
  }

  public static class Builder extends NearMediaBuilder<Builder, NearVideo> {
    public Builder(String video) {
      super(video);
    }

    @Override
    public final NearVideo build() {
      return new NearVideo(this);
    }
  }

  @Override
  public void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);
    req.setNearVideo(protoBuilder());
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req) {
    if (common.limit() != null) {
      req.setLimit(common.limit());
    }
    req.setNearVideo(protoBuilder());
  }

  private WeaviateProtoBaseSearch.NearVideoSearch.Builder protoBuilder() {
    var nearVideo = WeaviateProtoBaseSearch.NearVideoSearch.newBuilder();
    nearVideo.setVideo(video);

    if (certainty != null) {
      nearVideo.setCertainty(certainty);
    } else if (distance != null) {
      nearVideo.setDistance(distance);
    }
    return nearVideo;
  }
}
