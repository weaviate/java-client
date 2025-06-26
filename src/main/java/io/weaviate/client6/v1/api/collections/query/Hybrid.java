package io.weaviate.client6.v1.api.collections.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.aggregate.AggregateObjectFilter;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoAggregate;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBaseSearch;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record Hybrid(
    String query,
    List<String> queryProperties,
    SearchOperator searchOperator,
    Float alpha,
    QueryOperator near,
    FusionType fusionType,
    Float maxVectorDistance,
    BaseQueryOptions common)
    implements QueryOperator, AggregateObjectFilter {

  public static enum FusionType {
    RELATIVE_SCORE, RANKED;
  }

  public static final Hybrid of(String query) {
    return of(query, ObjectBuilder.identity());
  }

  public static final Hybrid of(String query, Function<Builder, ObjectBuilder<Hybrid>> fn) {
    return fn.apply(new Builder(query)).build();
  }

  public Hybrid(Builder builder) {
    this(
        builder.query,
        builder.queryProperties,
        builder.searchOperator,
        builder.alpha,
        builder.near,
        builder.fusionType,
        builder.maxVectorDistance,
        builder.baseOptions());
  }

  public static class Builder extends BaseQueryOptions.Builder<Builder, Hybrid> {
    // Required query parameters.
    private final String query;

    // Optional query parameters.
    List<String> queryProperties = new ArrayList<>();
    SearchOperator searchOperator;
    Float alpha;
    QueryOperator near;
    FusionType fusionType;
    Float maxVectorDistance;

    public Builder(String query) {
      this.query = query;
    }

    public Builder queryProperties(String... properties) {
      return queryProperties(Arrays.asList(properties));
    }

    public Builder queryProperties(List<String> properties) {
      this.queryProperties = properties;
      return this;
    }

    public Builder searchOperator(SearchOperator searchOperator) {
      this.searchOperator = searchOperator;
      return this;
    }

    public Builder alpha(float alpha) {
      this.alpha = alpha;
      return this;
    }

    public Builder fusionType(FusionType fusionType) {
      this.fusionType = fusionType;
      return this;
    }

    public Builder maxVectorDistance(float maxVectorDistance) {
      this.maxVectorDistance = maxVectorDistance;
      return this;
    }

    public Builder nearVector(NearVector nearVector) {
      this.near = nearVector;
      return this;
    }

    public Builder nearText(NearText nearText) {
      this.near = nearText;
      return this;
    }

    @Override
    public final Hybrid build() {
      return new Hybrid(this);
    }
  }

  @Override
  public void appendTo(WeaviateProtoAggregate.AggregateRequest.Builder req) {
    if (common.limit() != null) {
      req.setLimit(common.limit());
    }
    req.setHybrid(protoBuilder());
  }

  @Override
  public final void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);
    req.setHybridSearch(protoBuilder());
  }

  private WeaviateProtoBaseSearch.Hybrid.Builder protoBuilder() {
    var hybrid = WeaviateProtoBaseSearch.Hybrid.newBuilder()
        .setQuery(query)
        .addAllProperties(queryProperties);

    if (alpha != null) {
      hybrid.setAlpha(alpha);
    }

    if (fusionType != null) {
      switch (fusionType) {
        case RANKED:
          hybrid.setFusionType(WeaviateProtoBaseSearch.Hybrid.FusionType.FUSION_TYPE_RANKED);
        case RELATIVE_SCORE:
          hybrid.setFusionType(WeaviateProtoBaseSearch.Hybrid.FusionType.FUSION_TYPE_RELATIVE_SCORE);
      }
    }

    if (maxVectorDistance != null) {
      hybrid.setVectorDistance(maxVectorDistance);
    }

    if (near != null) {
      if (near instanceof NearVector nv) {
        hybrid.setNearVector(nv.protoBuilder());
      } else if (near instanceof NearText nt) {
        hybrid.setNearText(nt.protoBuilder());
      }
    }

    return hybrid;
  }
}
