package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.api.collections.query.Target.CombinedTextTarget;
import io.weaviate.client6.v1.api.collections.query.Target.TextTarget;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearVideo(Target searchTarget, Float distance, Float certainty, BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static NearVideo of(String video) {
    return of(Target.blob(video));
  }

  public static NearVideo of(String video, Function<Builder, ObjectBuilder<NearVideo>> fn) {
    return of(Target.blob(video), fn);
  }

  public static NearVideo of(Target searchTarget) {
    return of(searchTarget, ObjectBuilder.identity());
  }

  public static NearVideo of(Target searchTarget, Function<Builder, ObjectBuilder<NearVideo>> fn) {
    return fn.apply(new Builder(searchTarget)).build();
  }

  public NearVideo(Builder builder) {
    this(
        builder.media,
        builder.distance,
        builder.certainty,
        builder.baseOptions());
  }

  public static class Builder extends NearMediaBuilder<Builder, NearVideo> {
    public Builder(Target searchTarget) {
      super(searchTarget);
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
    if (searchTarget instanceof TextTarget video) {
      nearVideo.setVideo(video.query().get(0));
    } else if (searchTarget instanceof CombinedTextTarget combined) {
      nearVideo.setVideo(combined.query().get(0));
    }

    var targets = WeaviateProtoBaseSearch.Targets.newBuilder();
    if (searchTarget.appendTargets(targets)) {
      nearVideo.setTargets(targets);
    }

    if (certainty != null) {
      nearVideo.setCertainty(certainty);
    } else if (distance != null) {
      nearVideo.setDistance(distance);
    }
    return nearVideo;
  }
}
