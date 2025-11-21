package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.api.collections.query.Target.CombinedTextTarget;
import io.weaviate.client6.v1.api.collections.query.Target.TextTarget;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearDepth(
    Target searchTarget,
    Float distance,
    Float certainty,
    Rerank rerank,
    BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static NearDepth of(String depth) {
    return of(Target.blob(depth));
  }

  public static NearDepth of(String depth, Function<Builder, ObjectBuilder<NearDepth>> fn) {
    return of(Target.blob(depth), fn);
  }

  public static NearDepth of(Target searchTarget) {
    return of(searchTarget, ObjectBuilder.identity());
  }

  public static NearDepth of(Target searchTarget, Function<Builder, ObjectBuilder<NearDepth>> fn) {
    return fn.apply(new Builder(searchTarget)).build();
  }

  public NearDepth(Builder builder) {
    this(
        builder.media,
        builder.distance,
        builder.certainty,
        builder.rerank,
        builder.baseOptions());
  }

  public static class Builder extends NearMediaBuilder<Builder, NearDepth> {
    public Builder(Target searchTarget) {
      super(searchTarget);
    }

    @Override
    public final NearDepth build() {
      return new NearDepth(this);
    }
  }

  @Override
  public void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);
    req.setNearDepth(protoBuilder());
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req) {
    if (common.limit() != null) {
      req.setLimit(common.limit());
    }
    req.setNearDepth(protoBuilder());
  }

  private WeaviateProtoBaseSearch.NearDepthSearch.Builder protoBuilder() {
    var nearDepth = WeaviateProtoBaseSearch.NearDepthSearch.newBuilder();
    if (searchTarget instanceof TextTarget depth) {
      nearDepth.setDepth(depth.query().get(0));
    } else if (searchTarget instanceof CombinedTextTarget combined) {
      nearDepth.setDepth(combined.query().get(0));
    }

    var targets = WeaviateProtoBaseSearch.Targets.newBuilder();
    if (searchTarget.appendTargets(targets)) {
      nearDepth.setTargets(targets);
    }

    if (certainty != null) {
      nearDepth.setCertainty(certainty);
    } else if (distance != null) {
      nearDepth.setDistance(distance);
    }
    return nearDepth;
  }
}
