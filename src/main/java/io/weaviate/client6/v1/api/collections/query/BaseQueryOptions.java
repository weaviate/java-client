package io.weaviate.client6.v1.api.collections.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record BaseQueryOptions(
    Integer limit,
    Integer offset,
    Integer autocut,
    String after,
    String consistencyLevel,
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
    private String consistencyLevel;
    private List<String> returnProperties = new ArrayList<>();
    private List<QueryReference> returnReferences = new ArrayList<>();
    private List<Metadata> returnMetadata = new ArrayList<>();

    public final SELF limit(Integer limit) {
      this.limit = limit;
      return (SELF) this;
    }

    public final SELF offset(Integer offset) {
      this.offset = offset;
      return (SELF) this;
    }

    public final SELF autocut(Integer autocut) {
      this.autocut = autocut;
      return (SELF) this;
    }

    public final SELF after(String after) {
      this.after = after;
      return (SELF) this;
    }

    public final SELF returnProperties(String... properties) {
      this.returnProperties = Arrays.asList(properties);
      return (SELF) this;
    }

    public final SELF returnReferences(QueryReference references) {
      this.returnReferences = Arrays.asList(references);
      return (SELF) this;
    }

    public final SELF returnMetadata(Metadata... metadata) {
      this.returnMetadata = Arrays.asList(metadata);
      return (SELF) this;
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

    if (StringUtils.isNotBlank(consistencyLevel)) {
      req.setConsistencyLevelValue(Integer.valueOf(consistencyLevel));
    }

    if (!returnMetadata.isEmpty()) {
      var metadata = WeaviateProtoSearchGet.MetadataRequest.newBuilder();
      returnMetadata.forEach(m -> m.appendTo(metadata));
      req.setMetadata(metadata);
    }

    if (!returnProperties.isEmpty() || !returnReferences.isEmpty()) {
      var properties = WeaviateProtoSearchGet.PropertiesRequest.newBuilder();

      if (!returnProperties.isEmpty()) {
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
