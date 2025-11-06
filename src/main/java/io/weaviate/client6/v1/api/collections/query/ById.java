package io.weaviate.client6.v1.api.collections.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.query.Metadata.MetadataField;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record ById(
    String uuid,
    List<String> returnProperties,
    List<QueryReference> returnReferences,
    List<Metadata> returnMetadata,
    List<String> includeVectors) implements QueryOperator {

  static final String ID_PROPERTY = "_id";

  public static ById of(String uuid) {
    return of(uuid, ObjectBuilder.identity());
  }

  public static ById of(String uuid, Function<Builder, ObjectBuilder<ById>> fn) {
    return fn.apply(new Builder(uuid)).build();
  }

  public ById(Builder builder) {
    this(builder.uuid,
        new ArrayList<>(builder.returnProperties),
        builder.returnReferences,
        new ArrayList<>(builder.returnMetadata),
        builder.includeVectors);
  }

  public static class Builder implements ObjectBuilder<ById> {
    // Required query parameters.
    private final String uuid;

    private Set<String> returnProperties = new HashSet<>();
    private List<QueryReference> returnReferences = new ArrayList<>();
    private Set<Metadata> returnMetadata = new HashSet<>();
    private List<String> includeVectors = new ArrayList<>();

    public Builder(String uuid) {
      this.uuid = uuid;
      returnMetadata(MetadataField.UUID);
    }

    /** Select properties to include in the query result. */
    public final Builder returnProperties(String... properties) {
      return returnProperties(Arrays.asList(properties));
    }

    /** Select properties to include in the query result. */
    public final Builder returnProperties(List<String> properties) {
      this.returnProperties.addAll(properties);
      return this;
    }

    /** Select cross-referenced objects to include in the query result. */
    public final Builder returnReferences(QueryReference... references) {
      return returnReferences(Arrays.asList(references));
    }

    /** Select cross-referenced objects to include in the query result. */
    public final Builder returnReferences(List<QueryReference> references) {
      this.returnReferences.addAll(references);
      return this;
    }

    /** Select metadata to include in the query result. */
    public final Builder returnMetadata(Metadata... metadata) {
      return returnMetadata(Arrays.asList(metadata));
    }

    /** Select metadata to include in the query result. */
    public final Builder returnMetadata(List<Metadata> metadata) {
      this.returnMetadata.addAll(metadata);
      return this;
    }

    /** Include default vector. */
    public final Builder includeVector() {
      return returnMetadata(MetadataField.VECTOR);
    }

    /** Include one or more named vectors in the metadata response. */
    public final Builder includeVector(String... vectors) {
      return includeVector(Arrays.asList(vectors));
    }

    /** Include one or more named vectors in the metadata response. */
    public final Builder includeVector(List<String> vectors) {
      this.includeVectors.addAll(vectors);
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
    returnMetadata.forEach(m -> m.appendTo(metadata));
    metadata.addAllVectors(includeVectors);
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
