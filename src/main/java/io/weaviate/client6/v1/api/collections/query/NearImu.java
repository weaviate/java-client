package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.api.collections.query.Target.CombinedTextTarget;
import io.weaviate.client6.v1.api.collections.query.Target.TextTarget;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearImu(Target searchTarget, Float distance, Float certainty, BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static NearImu of(String imu) {
    return of(Target.blob(imu));
  }

  public static NearImu of(String imu, Function<Builder, ObjectBuilder<NearImu>> fn) {
    return of(Target.blob(imu), fn);
  }

  public static NearImu of(Target searchTarget) {
    return of(searchTarget, ObjectBuilder.identity());
  }

  public static NearImu of(Target searchTarget, Function<Builder, ObjectBuilder<NearImu>> fn) {
    return fn.apply(new Builder(searchTarget)).build();
  }

  public NearImu(Builder builder) {
    this(
        builder.media,
        builder.distance,
        builder.certainty,
        builder.baseOptions());
  }

  public static class Builder extends NearMediaBuilder<Builder, NearImu> {
    public Builder(Target searchTarget) {
      super(searchTarget);
    }

    @Override
    public final NearImu build() {
      return new NearImu(this);
    }
  }

  @Override
  public void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);
    req.setNearImu(protoBuilder());
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req) {
    if (common.limit() != null) {
      req.setLimit(common.limit());
    }
    req.setNearImu(protoBuilder());
  }

  private WeaviateProtoBaseSearch.NearIMUSearch.Builder protoBuilder() {
    var nearImu = WeaviateProtoBaseSearch.NearIMUSearch.newBuilder();
    if (searchTarget instanceof TextTarget imu) {
      nearImu.setImu(imu.query().get(0));
    } else if (searchTarget instanceof CombinedTextTarget combined) {
      nearImu.setImu(combined.query().get(0));
    }

    var targets = WeaviateProtoBaseSearch.Targets.newBuilder();
    if (searchTarget.appendTargets(targets)) {
      nearImu.setTargets(targets);
    }

    if (certainty != null) {
      nearImu.setCertainty(certainty);
    } else if (distance != null) {
      nearImu.setDistance(distance);
    }
    return nearImu;
  }
}
