package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearDepth(String depth, Float distance, Float certainty, BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static NearDepth of(String depth) {
    return of(depth, ObjectBuilder.identity());
  }

  public static NearDepth of(String depth, Function<Builder, ObjectBuilder<NearDepth>> fn) {
    return fn.apply(new Builder(depth)).build();
  }

  public NearDepth(Builder builder) {
    this(
        builder.media,
        builder.distance,
        builder.certainty,
        builder.baseOptions());
  }

  public static class Builder extends NearMediaBuilder<Builder, NearDepth> {
    public Builder(String depth) {
      super(depth);
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
    nearDepth.setDepth(depth);

    if (certainty != null) {
      nearDepth.setCertainty(certainty);
    } else if (distance != null) {
      nearDepth.setDistance(distance);
    }
    return nearDepth;
  }
}
