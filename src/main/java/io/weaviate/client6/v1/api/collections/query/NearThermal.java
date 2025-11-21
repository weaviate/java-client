package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.api.collections.query.Target.CombinedTextTarget;
import io.weaviate.client6.v1.api.collections.query.Target.TextTarget;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearThermal(Target searchTarget,
    Float distance,
    Float certainty,
    Rerank rerank,
    BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static NearThermal of(String thermal) {
    return of(Target.blob(thermal));
  }

  public static NearThermal of(String thermal, Function<Builder, ObjectBuilder<NearThermal>> fn) {
    return of(Target.blob(thermal), fn);
  }

  public static NearThermal of(Target searchTarget) {
    return of(searchTarget, ObjectBuilder.identity());
  }

  public static NearThermal of(Target searchTarget, Function<Builder, ObjectBuilder<NearThermal>> fn) {
    return fn.apply(new Builder(searchTarget)).build();
  }

  public NearThermal(Builder builder) {
    this(
        builder.media,
        builder.distance,
        builder.certainty,
        builder.rerank,
        builder.baseOptions());
  }

  public static class Builder extends NearMediaBuilder<Builder, NearThermal> {
    public Builder(Target searchTarget) {
      super(searchTarget);
    }

    @Override
    public final NearThermal build() {
      return new NearThermal(this);
    }
  }

  @Override
  public void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);
    req.setNearThermal(protoBuilder());
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req) {
    if (common.limit() != null) {
      req.setLimit(common.limit());
    }
    req.setNearThermal(protoBuilder());
  }

  private WeaviateProtoBaseSearch.NearThermalSearch.Builder protoBuilder() {
    var nearThermal = WeaviateProtoBaseSearch.NearThermalSearch.newBuilder();
    if (searchTarget instanceof TextTarget thermal) {
      nearThermal.setThermal(thermal.query().get(0));
    } else if (searchTarget instanceof CombinedTextTarget combined) {
      nearThermal.setThermal(combined.query().get(0));
    }

    var targets = WeaviateProtoBaseSearch.Targets.newBuilder();
    if (searchTarget.appendTargets(targets)) {
      nearThermal.setTargets(targets);
    }

    if (certainty != null) {
      nearThermal.setCertainty(certainty);
    } else if (distance != null) {
      nearThermal.setDistance(distance);
    }
    return nearThermal;
  }
}
