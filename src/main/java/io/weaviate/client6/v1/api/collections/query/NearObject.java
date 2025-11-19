package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearObject(String uuid, Float distance, Float certainty, BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static final NearObject of(String uuid) {
    return of(uuid, ObjectBuilder.identity());
  }

  public static final NearObject of(String uuid, Function<Builder, ObjectBuilder<NearObject>> fn) {
    return fn.apply(new Builder(uuid)).build();
  }

  public NearObject(Builder builder) {
    this(builder.uuid, builder.distance, builder.certainty, builder.baseOptions());
  }

  public static class Builder extends BaseVectorSearchBuilder<Builder, NearObject> {
    // Required query parameters.
    private final String uuid;

    public Builder(String uuid) {
      this.uuid = uuid;
    }

    public Builder excludeSelf() {
      return filters(Filter.uuid().ne(uuid));
    }

    @Override
    public final NearObject build() {
      return new NearObject(this);
    }
  }

  @Override
  public final void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);
    req.setNearObject(protoBuilder());
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req) {
    if (common.limit() != null) {
      req.setLimit(common.limit());
    }
    req.setNearObject(protoBuilder());
  }

  private WeaviateProtoBaseSearch.NearObject.Builder protoBuilder() {
    var nearObject = WeaviateProtoBaseSearch.NearObject.newBuilder()
        .setId(uuid);

    if (certainty != null) {
      nearObject.setCertainty(certainty);
    } else if (distance != null) {
      nearObject.setDistance(distance);
    }
    return nearObject;
  }
}
