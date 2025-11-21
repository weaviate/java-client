package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearVector(NearVectorTarget searchTarget,
    Float distance,
    Float certainty,
    Rerank rerank,
    BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static final NearVector of(float[] vector) {
    return of(vector, ObjectBuilder.identity());
  }

  public static final NearVector of(float[] vector, Function<Builder, ObjectBuilder<NearVector>> fn) {
    return fn.apply(new Builder(Target.vector(vector))).build();
  }

  public static final NearVector of(float[][] vector) {
    return of(vector, ObjectBuilder.identity());
  }

  public static final NearVector of(float[][] vector, Function<Builder, ObjectBuilder<NearVector>> fn) {
    return fn.apply(new Builder(Target.vector(vector))).build();
  }

  public static final NearVector of(NearVectorTarget searchTarget) {
    return of(searchTarget, ObjectBuilder.identity());
  }

  public static final NearVector of(NearVectorTarget searchTarget, Function<Builder, ObjectBuilder<NearVector>> fn) {
    return fn.apply(new Builder(searchTarget)).build();
  }

  public NearVector(Builder builder) {
    this(builder.searchTarget,
        builder.distance,
        builder.certainty,
        builder.rerank,
        builder.baseOptions());
  }

  public static class Builder extends BaseVectorSearchBuilder<Builder, NearVector> {
    // Required query parameters.
    private final NearVectorTarget searchTarget;

    public Builder(NearVectorTarget searchTarget) {
      this.searchTarget = searchTarget;
    }

    @Override
    public final NearVector build() {
      return new NearVector(this);
    }
  }

  @Override
  public final void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);
    req.setNearVector(protoBuilder(true));
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req) {
    if (common.limit() != null) {
      req.setLimit(common.limit());
    }
    req.setNearVector(protoBuilder(true));
  }

  WeaviateProtoBaseSearch.NearVector.Builder protoBuilder(boolean withTargets) {
    var nearVector = WeaviateProtoBaseSearch.NearVector.newBuilder();

    searchTarget.appendVectors(nearVector);

    var targets = WeaviateProtoBaseSearch.Targets.newBuilder();
    if (withTargets && searchTarget.appendTargets(targets)) {
      nearVector.setTargets(targets);
    }

    if (certainty != null) {
      nearVector.setCertainty(certainty);
    } else if (distance != null) {
      nearVector.setDistance(distance);
    }
    return nearVector;
  }
}
