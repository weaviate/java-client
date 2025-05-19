package io.weaviate.client6.v1.api.collections.query;

import java.util.function.Function;

import io.weaviate.client6.internal.GRPC;
import io.weaviate.client6.v1.api.collections.aggregate.ObjectFilter;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record NearVector(Float[] vector, Float distance, Float certainty, BaseQueryOptions common)
    implements SearchOperator, ObjectFilter {

  public static final NearVector of(Float[] vector) {
    return of(vector, ObjectBuilder.identity());
  }

  public static final NearVector of(Float[] vector, Function<Builder, ObjectBuilder<NearVector>> fn) {
    return fn.apply(new Builder(vector)).build();
  }

  public NearVector(Builder builder) {
    this(builder.vector, builder.distance, builder.certainty, builder.baseOptions());
  }

  public static class Builder extends BaseQueryOptions.Builder<Builder, NearVector> {
    // Required query parameters.
    private final Float[] vector;

    // Optional query parameters.
    private Float distance;
    private Float certainty;

    public Builder(Float[] vector) {
      this.vector = vector;
    }

    public final Builder distance(float distance) {
      this.distance = distance;
      return this;
    }

    public final Builder certainty(float certainty) {
      this.certainty = certainty;
      return this;
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

  private WeaviateProtoBaseSearch.NearVector.Builder protoBuilder() {
    var nearVector = WeaviateProtoBaseSearch.NearVector.newBuilder();
    nearVector.addVectors(WeaviateProtoBase.Vectors.newBuilder()
        .setType(WeaviateProtoBase.Vectors.VectorType.VECTOR_TYPE_SINGLE_FP32)
        .setVectorBytes(GRPC.toByteString(vector)));

    if (certainty != null) {
      nearVector.setCertainty(certainty);
    } else if (distance != null) {
      nearVector.setDistance(distance);
    }
    return nearVector;
  }
}
