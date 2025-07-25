package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.ByteStringUtil;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearVector(float[] vector, Float distance, Float certainty, BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static final NearVector of(float[] vector) {
    return of(vector, ObjectBuilder.identity());
  }

  public static final NearVector of(float[] vector, Function<Builder, ObjectBuilder<NearVector>> fn) {
    return fn.apply(new Builder(vector)).build();
  }

  public NearVector(Builder builder) {
    this(builder.vector, builder.distance, builder.certainty, builder.baseOptions());
  }

  public static class Builder extends BaseVectorSearchBuilder<Builder, NearVector> {
    // Required query parameters.
    private final float[] vector;

    public Builder(float[] vector) {
      this.vector = vector;
    }

    @Override
    public final NearVector build() {
      return new NearVector(this);
    }
  }

  @Override
  public final void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);
    req.setNearVector(protoBuilder());
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req) {
    if (common.limit() != null) {
      req.setLimit(common.limit());
    }
    req.setNearVector(protoBuilder());
  }

  // This is made package-private for Hybrid to see. Should we refactor?
  WeaviateProtoBaseSearch.NearVector.Builder protoBuilder() {
    var nearVector = WeaviateProtoBaseSearch.NearVector.newBuilder();
    nearVector.addVectors(WeaviateProtoBase.Vectors.newBuilder()
        .setType(WeaviateProtoBase.Vectors.VectorType.VECTOR_TYPE_SINGLE_FP32)
        .setVectorBytes(ByteStringUtil.encodeVectorSingle(vector)));

    if (certainty != null) {
      nearVector.setCertainty(certainty);
    } else if (distance != null) {
      nearVector.setDistance(distance);
    }
    return nearVector;
  }
}
