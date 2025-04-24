package io.weaviate.client6.v1.collections.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataRequest;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.PropertiesRequest;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;

@SuppressWarnings("unchecked")
public record CommonQueryOptions(
    Integer limit,
    Integer offset,
    Integer autocut,
    String after,
    String consistencyLevel /* TODO: use ConsistencyLevel enum */,
    List<String> returnProperties,
    List<QueryReference> returnReferences,
    List<Metadata> returnMetadata) {

  public CommonQueryOptions(Builder<? extends Builder<?>> options) {
    this(
        options.limit,
        options.offset,
        options.autocut,
        options.after,
        options.consistencyLevel,
        options.returnProperties,
        options.returnReferences,
        options.returnMetadata);

  }

  public static abstract class Builder<SELF extends Builder<SELF>> {
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

    public final SELF consistencyLevel(String consistencyLevel) {
      this.consistencyLevel = consistencyLevel;
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

    void appendTo(SearchRequest.Builder search) {
      if (limit != null) {
        search.setLimit(limit);
      }
      if (offset != null) {
        search.setOffset(offset);
      }
      if (StringUtils.isNotBlank(after)) {
        search.setAfter(after);
      }
      if (StringUtils.isNotBlank(consistencyLevel)) {
        search.setConsistencyLevelValue(Integer.valueOf(consistencyLevel));
      }
      if (autocut != null) {
        search.setAutocut(autocut);
      }

      if (!returnMetadata.isEmpty()) {
        var metadata = MetadataRequest.newBuilder();
        returnMetadata.forEach(m -> m.appendTo(metadata));
        search.setMetadata(metadata);
      }

      if (!returnProperties.isEmpty()) {
        var properties = PropertiesRequest.newBuilder();
        for (String property : returnProperties) {
          properties.addNonRefProperties(property);
        }
        search.setProperties(properties);
      }
    }
  }
}
