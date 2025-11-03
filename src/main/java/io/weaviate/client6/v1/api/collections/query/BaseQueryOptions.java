package io.weaviate.client6.v1.api.collections.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import io.weaviate.client6.v1.api.collections.query.Metadata.MetadataField;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoBase;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record BaseQueryOptions(
    Integer limit,
    Integer offset,
    Integer autocut,
    String after,
    ConsistencyLevel consistencyLevel,
    Where where,
    GenerativeSearch generativeSearch,
    List<String> returnProperties,
    List<QueryReference> returnReferences,
    List<Metadata> returnMetadata,
    List<String> includeVectors) {

  private <T extends Object> BaseQueryOptions(Builder<? extends Builder<?, T>, T> builder) {
    this(
        builder.limit,
        builder.offset,
        builder.autocut,
        builder.after,
        builder.consistencyLevel,
        builder.where,
        builder.generativeSearch,
        builder.returnProperties,
        builder.returnReferences,
        builder.returnMetadata,
        builder.includeVectors);

  }

  @SuppressWarnings("unchecked")
  public static abstract class Builder<SelfT extends Builder<SelfT, T>, T extends Object> implements ObjectBuilder<T> {
    private Integer limit;
    private Integer offset;
    private Integer autocut;
    private String after;
    private ConsistencyLevel consistencyLevel;
    private Where where;
    private GenerativeSearch generativeSearch;
    private List<String> returnProperties = new ArrayList<>();
    private List<QueryReference> returnReferences = new ArrayList<>();
    private List<Metadata> returnMetadata = new ArrayList<>();
    private List<String> includeVectors = new ArrayList<>();

    protected Builder() {
      returnMetadata(MetadataField.UUID);
    }

    /**
     * Limit the number of returned objects.
     *
     * <p>
     * Combine with {@link #offset(int)} to use offset-based pagination.
     */
    public final SelfT limit(int limit) {
      this.limit = limit;
      return (SelfT) this;
    }

    /**
     * Skip the first N objects in the result set.
     *
     * <p>
     * Combine with {@link #limit(int)} to use offset-based pagination.
     */
    public final SelfT offset(int offset) {
      this.offset = offset;
      return (SelfT) this;
    }

    /**
     * Discard results after an automatically calculated cutoff point.
     *
     * @param autocut The number of "groups" to keep.
     * @see <a href=
     *      "https://weaviate.io/learn/knowledgecards/autocut">Documentation</a>
     */
    public final SelfT autocut(int autocut) {
      this.autocut = autocut;
      return (SelfT) this;
    }

    /**
     * Discard results before this object.
     *
     * @param after UUID of an object in this collection.
     */
    public final SelfT after(String after) {
      this.after = after;
      return (SelfT) this;
    }

    /** Set consitency level for query resolution. */
    public final SelfT consistencyLevel(ConsistencyLevel consistencyLevel) {
      this.consistencyLevel = consistencyLevel;
      return (SelfT) this;
    }

    /**
     * Add arguments for generative query.
     * Builders which support this parameter should make the method public.
     *
     * @param fn Lambda expression for optional parameters.
     */
    protected SelfT generate(Function<GenerativeSearch.Builder, ObjectBuilder<GenerativeSearch>> fn) {
      this.generativeSearch = GenerativeSearch.of(fn);
      return (SelfT) this;
    }

    /**
     * Filter result set using traditional filtering operators: {@code eq},
     * {@code gte}, {@code like}, etc.
     * Subsequent calls to {@link #where} aggregate with an AND operator.
     */
    public final SelfT where(Where where) {
      this.where = this.where == null ? where : Where.and(this.where, where);
      return (SelfT) this;
    }

    /** Combine several conditions using with an AND operator. */
    public final SelfT where(Where... wheres) {
      Arrays.stream(wheres).map(this::where);
      return (SelfT) this;
    }

    /** Select properties to include in the query result. */
    public final SelfT returnProperties(String... properties) {
      return returnProperties(Arrays.asList(properties));
    }

    /** Select properties to include in the query result. */
    public final SelfT returnProperties(List<String> properties) {
      this.returnProperties.addAll(properties);
      return (SelfT) this;
    }

    /** Select cross-referenced objects to include in the query result. */
    public final SelfT returnReferences(QueryReference... references) {
      return returnReferences(Arrays.asList(references));
    }

    /** Select cross-referenced objects to include in the query result. */
    public final SelfT returnReferences(List<QueryReference> references) {
      this.returnReferences.addAll(references);
      return (SelfT) this;
    }

    /** Select metadata to include in the query result. */
    public final SelfT returnMetadata(Metadata... metadata) {
      return returnMetadata(Arrays.asList(metadata));
    }

    /** Select metadata to include in the query result. */
    public final SelfT returnMetadata(List<Metadata> metadata) {
      this.returnMetadata.addAll(metadata);
      return (SelfT) this;
    }

    /** Include default vector. */
    public final SelfT includeVector() {
      return returnMetadata(Metadata.VECTOR);
    }

    /** Include one or more named vectors in the metadata response. */
    public final SelfT includeVector(String... vectors) {
      return includeVector(Arrays.asList(vectors));
    }

    /** Include one or more named vectors in the metadata response. */
    public final SelfT includeVector(List<String> vectors) {
      this.includeVectors.addAll(vectors);
      return (SelfT) this;
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

    if (generativeSearch != null) {
      var generative = WeaviateProtoGenerative.GenerativeSearch.newBuilder();
      generativeSearch.appendTo(generative);
      req.setGenerative(generative);
    }

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
