package io.weaviate.client6.v1.api.collections.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record ById(
    String uuid,
    List<String> returnProperties,
    List<QueryReference> returnReferences,
    List<Metadata> returnMetadata) implements QueryOperator {

  static final String ID_PROPERTY = "_id";

  public static ById of(String uuid) {
    return of(uuid, ObjectBuilder.identity());
  }

  public static ById of(String uuid, Function<Builder, ObjectBuilder<ById>> fn) {
    return fn.apply(new Builder(uuid)).build();
  }

  public ById(Builder builder) {
    this(builder.uuid,
        builder.returnProperties,
        builder.returnReferences,
        builder.returnMetadata);
  }

  public static class Builder implements ObjectBuilder<ById> {
    // Required query parameters.
    private final String uuid;

    private List<String> returnProperties = new ArrayList<>();
    private List<QueryReference> returnReferences = new ArrayList<>();
    private List<Metadata> returnMetadata = new ArrayList<>();

    public Builder(String uuid) {
      this.uuid = uuid;
    }

    public final Builder returnProperties(String... properties) {
      this.returnProperties = Arrays.asList(properties);
      return this;
    }

    public final Builder returnReferences(QueryReference... references) {
      this.returnReferences = Arrays.asList(references);
      return this;
    }

    public final Builder returnMetadata(Metadata... metadata) {
      this.returnMetadata = Arrays.asList(metadata);
      return this;
    }

    @Override
    public ById build() {
      return new ById(this);
    }
  }

  @Override
  public void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    var where = Where.uuid().eq(uuid);
    var filter = WeaviateProtoBase.Filters.newBuilder();
    where.appendTo(filter);
    req.setFilters(filter);

    var metadata = WeaviateProtoSearchGet.MetadataRequest.newBuilder();
    if (returnMetadata.isEmpty()) {
      returnMetadata.add(Metadata.UUID);
    }
    returnMetadata.forEach(m -> m.appendTo(metadata));
    req.setMetadata(metadata);

    if (!returnProperties.isEmpty() || !returnReferences.isEmpty()) {
      var properties = WeaviateProtoSearchGet.PropertiesRequest.newBuilder();

      if (returnProperties.isEmpty()) {
        properties.setReturnAllNonrefProperties(true);
      } else {
        properties.addAllNonRefProperties(returnProperties);
      }

      if (!returnReferences.isEmpty()) {
        returnReferences.forEach(r -> {
          var ref = WeaviateProtoSearchGet.RefPropertiesRequest.newBuilder();
          r.appendTo(ref);
          properties.addRefProperties(ref);
        });
      }
      req.setProperties(properties);
    }
  }
}
