package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearThermal(String thermal, Float distance, Float certainty, BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static NearThermal of(String thermal) {
    return of(thermal, ObjectBuilder.identity());
  }

  public static NearThermal of(String thermal, Function<Builder, ObjectBuilder<NearThermal>> fn) {
    return fn.apply(new Builder(thermal)).build();
  }

  public NearThermal(Builder builder) {
    this(
        builder.media,
        builder.distance,
        builder.certainty,
        builder.baseOptions());
  }

  public static class Builder extends NearMediaBuilder<Builder, NearThermal> {
    public Builder(String thermal) {
      super(thermal);
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
    nearThermal.setThermal(thermal);

    if (certainty != null) {
      nearThermal.setCertainty(certainty);
    } else if (distance != null) {
      nearThermal.setDistance(distance);
    }
    return nearThermal;
  }
}
