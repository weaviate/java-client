package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.api.collections.query.Target.CombinedTextTarget;
import io.weaviate.client6.v1.api.collections.query.Target.TextTarget;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearImage(
    Target searchTarget,
    Float distance,
    Float certainty,
    Rerank rerank,
    BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static NearImage of(String image) {
    return of(Target.blob(image));
  }

  public static NearImage of(String image, Function<Builder, ObjectBuilder<NearImage>> fn) {
    return of(Target.blob(image), fn);
  }

  public static NearImage of(Target searchTarget) {
    return of(searchTarget, ObjectBuilder.identity());
  }

  public static NearImage of(Target searchTarget, Function<Builder, ObjectBuilder<NearImage>> fn) {
    return fn.apply(new Builder(searchTarget)).build();
  }

  public NearImage(Builder builder) {
    this(
        builder.media,
        builder.distance,
        builder.certainty,
        builder.rerank,
        builder.baseOptions());
  }

  public static class Builder extends NearMediaBuilder<Builder, NearImage> {
    public Builder(Target searchTarget) {
      super(searchTarget);
    }

    @Override
    public final NearImage build() {
      return new NearImage(this);
    }
  }

  @Override
  public void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);
    req.setNearImage(protoBuilder());
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req) {
    if (common.limit() != null) {
      req.setLimit(common.limit());
    }
    req.setNearImage(protoBuilder());
  }

  private WeaviateProtoBaseSearch.NearImageSearch.Builder protoBuilder() {
    var nearImage = WeaviateProtoBaseSearch.NearImageSearch.newBuilder();
    if (searchTarget instanceof TextTarget image) {
      nearImage.setImage(image.query().get(0));
    } else if (searchTarget instanceof CombinedTextTarget combined) {
      nearImage.setImage(combined.query().get(0));
    }

    var targets = WeaviateProtoBaseSearch.Targets.newBuilder();
    if (searchTarget.appendTargets(targets)) {
      nearImage.setTargets(targets);
    }

    if (certainty != null) {
      nearImage.setCertainty(certainty);
    } else if (distance != null) {
      nearImage.setDistance(distance);
    }
    return nearImage;
  }
}
