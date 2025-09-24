package io.weaviate.client6.v1.api.collections.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.weaviate.client6.v1.api.collections.query.Metadata.MetadataField;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record BaseQueryOptions(
    Integer limit,
    Integer offset,
    Integer autocut,
    String after,
    ConsistencyLevel consistencyLevel,
    Where where,
    List<String> returnProperties,
    List<QueryReference> returnReferences,
    List<Metadata> returnMetadata) {

  private <T extends Object> BaseQueryOptions(Builder<? extends Builder<?, T>, T> builder) {
    this(
        builder.limit,
        builder.offset,
        builder.autocut,
        builder.after,
        builder.consistencyLevel,
        builder.where,
        builder.returnProperties,
        builder.returnReferences,
        builder.returnMetadata);

  }

  @SuppressWarnings("unchecked")
  public static abstract class Builder<SELF extends Builder<SELF, T>, T extends Object> implements ObjectBuilder<T> {
    private Integer limit;
    private Integer offset;
    private Integer autocut;
    private String after;
    private ConsistencyLevel consistencyLevel;
    private Where where;
    private List<String> returnProperties = new ArrayList<>();
    private List<QueryReference> returnReferences = new ArrayList<>();
    private List<Metadata> returnMetadata = new ArrayList<>();

    protected Builder() {
      returnMetadata(MetadataField.UUID);
    }

    public final SELF limit(int limit) {
      this.limit = limit;
      return (SELF) this;
    }

    public final SELF offset(int offset) {
      this.offset = offset;
      return (SELF) this;
    }

    public final SELF autocut(int autocut) {
      this.autocut = autocut;
      return (SELF) this;
    }

    public final SELF after(String after) {
      this.after = after;
      return (SELF) this;
    }

    public final SELF consistencyLevel(ConsistencyLevel consistencyLevel) {
      this.consistencyLevel = consistencyLevel;
      return (SELF) this;
    }

    /**
     * Filter result set using traditional filtering operators: {@code eq},
     * {@code gte}, {@code like}, etc.
     * Subsequent calls to {@link #where} aggregate with an AND operator.
     *
     * @see Where
     */
    public final SELF where(Where where) {
      this.where = this.where == null ? where : Where.and(this.where, where);
      return (SELF) this;
    }

    /** Combine several conditions using with an AND operator. */
    public final SELF where(Where... wheres) {
      Arrays.stream(wheres).map(this::where);
      return (SELF) this;
    }

    public final SELF returnProperties(String... properties) {
      return returnProperties(Arrays.asList(properties));
    }

    public final SELF returnProperties(List<String> properties) {
      this.returnProperties.addAll(properties);
      return (SELF) this;
    }

    public final SELF returnReferences(QueryReference... references) {
      return returnReferences(Arrays.asList(references));
    }

    public final SELF returnReferences(List<QueryReference> references) {
      this.returnReferences.addAll(references);
      return (SELF) this;
    }

    public final SELF returnMetadata(Metadata... metadata) {
      return returnMetadata(Arrays.asList(metadata));
    }

    public final SELF returnMetadata(List<Metadata> metadata) {
      this.returnMetadata.addAll(metadata);
      return (SELF) this;
    }

    public final SELF includeVector() {
      return returnMetadata(Metadata.VECTOR);
    }

    final BaseQueryOptions baseOptions() {
      return _build();
    }

    @Override
    public T build() {
      return (T) _build();
    }

    private BaseQueryOptions _build() {
      return new BaseQueryOptions(this);
    }
  }

  final void appendTo(WeaviateProtoSearchGet.SearchRequest.Builder req) {
    if (limit != null) {
      req.setLimit(limit);
    }
    if (offset != null) {
      req.setOffset(offset);
    }
    if (StringUtils.isNotBlank(after)) {
      req.setAfter(after);
    }
    if (autocut != null) {
      req.setAutocut(autocut);
    }

    if (consistencyLevel != null) {
      consistencyLevel.appendTo(req);
    }

    if (where != null) {
      var filter = WeaviateProtoBase.Filters.newBuilder();
      where.appendTo(filter);
      req.setFilters(filter);
    }

    var metadata = WeaviateProtoSearchGet.MetadataRequest.newBuilder();
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
