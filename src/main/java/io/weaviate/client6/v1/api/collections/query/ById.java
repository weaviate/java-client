package io.weaviate.client6.v1.api.collections.query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase.Filters.Operator;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record ById(
    String uuid,
    boolean includeVector,
    List<String> includeVectors,
    BaseQueryOptions common) implements SearchOperator {

  private static final String ID_PROPERTY = "_id";

  public static ById of(String uuid) {
    return of(uuid, ObjectBuilder.identity());
  }

  public static ById of(String uuid, Function<Builder, ObjectBuilder<ById>> fn) {
    return fn.apply(new Builder(uuid)).build();
  }

  public ById(Builder builder) {
    this(builder.uuid, builder.includeVector, builder.includeVectors, builder.baseOptions());
  }

  public static class Builder extends BaseQueryOptions.Builder<Builder, ById> {
    // Required query parameters.
    private final String uuid;

    private boolean includeVector = false;
    private List<String> includeVectors = new ArrayList<>();

    public Builder(String uuid) {
      this.uuid = uuid;
    }

    public final Builder includeVector(boolean include) {
      this.includeVector = include;
      return this;
    }

    @Override
    public final ById build() {
      return new ById(this);
    }
  }

  @Override
  public void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    common.appendTo(req);

    // Always request UUID back in this request.
    var metadata = WeaviateProtoSearchGet.MetadataRequest.newBuilder()
        .setUuid(true);
    if (includeVector) {
      metadata.setVector(true);
    } else if (!includeVectors.isEmpty()) {
      metadata.addAllVectors(includeVectors);
    }
    req.setMetadata(metadata);

    req.setFilters(WeaviateProtoBase.Filters.newBuilder()
        .setTarget(WeaviateProtoBase.FilterTarget.newBuilder()
            .setProperty(ID_PROPERTY))
        .setValueText(uuid)
        .setOperator(Operator.OPERATOR_EQUAL));
  }
}
