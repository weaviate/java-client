package io.weaviate.client.v1.experimental;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.weaviate.client.grpc.protocol.v1.WeaviateProtoBase.Filters;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataRequest;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.PropertiesRequest;
import io.weaviate.client.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;

@SuppressWarnings("unchecked")
public abstract class SearchOptions<SELF extends SearchOptions<SELF>> {
  private Integer limit;
  private Integer offset;
  private Integer autocut;
  private String after;
  private String consistencyLevel;
  private Where where;
  private List<String> returnProperties = new ArrayList<>();
  private List<Metadata> returnMetadata = new ArrayList<>();

  void append(SearchRequest.Builder search) {
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

    if (where != null) {
      Filters.Builder filters = Filters.newBuilder();
      where.append(filters);
      search.setFilters(filters.build());
    }

    if (!returnMetadata.isEmpty()) {
      MetadataRequest.Builder metadata = MetadataRequest.newBuilder();
      returnMetadata.forEach(m -> m.append(metadata));
      search.setMetadata(metadata.build());
    }

    if (!returnProperties.isEmpty()) {
      PropertiesRequest.Builder properties = PropertiesRequest.newBuilder();
      for (String property : returnProperties) {
        properties.addNonRefProperties(property);
      }
      search.setProperties(properties.build());
    }
  }

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

  public final SELF where(Where where) {
    this.where = where;
    return (SELF) this;
  }

  @SafeVarargs
  public final SELF returnProperties(String... properties) {
    this.returnProperties = Arrays.asList(properties);
    return (SELF) this;
  }

  @SafeVarargs
  public final SELF returnMetadata(Metadata... metadata) {
    this.returnMetadata = Arrays.asList(metadata);
    return (SELF) this;
  }
}
