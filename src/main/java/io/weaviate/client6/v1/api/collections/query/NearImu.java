package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearImu(String imu, Float distance, Float certainty, BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static NearImu of(String imu) {
    return of(imu, ObjectBuilder.identity());
  }

  public static NearImu of(String imu, Function<Builder, ObjectBuilder<NearImu>> fn) {
    return fn.apply(new Builder(imu)).build();
  }

  public NearImu(Builder builder) {
    this(
        builder.media,
        builder.distance,
        builder.certainty,
        builder.baseOptions());
  }

  public static class Builder extends NearMediaBuilder<Builder, NearImu> {
    public Builder(String imu) {
      super(imu);
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
    nearImu.setImu(imu);

    if (certainty != null) {
      nearImu.setCertainty(certainty);
    } else if (distance != null) {
      nearImu.setDistance(distance);
    }
    return nearImu;
  }
}
