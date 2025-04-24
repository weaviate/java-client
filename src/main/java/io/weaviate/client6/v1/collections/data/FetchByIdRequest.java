package io.weaviate.client6.v1.collections.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBase.FilterTarget;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBase.Filters;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBase.Filters.Operator;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.PropertiesRequest;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.RefPropertiesRequest;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.SearchRequest;
import io.weaviate.client6.v1.collections.query.QueryReference;

public record FetchByIdRequest(
    String collection,
    String id,
    boolean includeVector,
    List<String> includeVectors,
    List<String> returnProperties,
    List<QueryReference> returnReferences) {

  public FetchByIdRequest(Builder options) {
    this(
        options.collection,
        options.uuid,
        options.includeVector,
        options.includeVectors,
        options.returnProperties,
        options.returnReferences);
  }

  public static FetchByIdRequest of(String collection, String uuid, Consumer<Builder> fn) {
    var builder = new Builder(collection, uuid);
    fn.accept(builder);
    return new FetchByIdRequest(builder);
  }

  public static class Builder {
    private final String collection;
    private final String uuid;

    public Builder(String collection, String uuid) {
      this.collection = collection;
      this.uuid = uuid;
    }

    private boolean includeVector;
    private List<String> includeVectors;
    private List<String> returnProperties = new ArrayList<>();
    private List<QueryReference> returnReferences = new ArrayList<>();

    public final Builder includeVector() {
      this.includeVector = true;
      return this;
    }

    public final Builder includeVectors(String... vectors) {
      this.includeVectors = Arrays.asList(vectors);
      return this;
    }

    public final Builder returnProperties(String... properties) {
      this.returnProperties = Arrays.asList(properties);
      return this;
    }

    public final Builder returnReferences(QueryReference... references) {
      this.returnReferences = Arrays.asList(references);
      return this;
    }

  }

  void appendTo(SearchRequest.Builder req) {
    req.setLimit(1);
    req.setCollection(collection);
    var filter = Filters.newBuilder();
    var target = FilterTarget.newBuilder();
    target.setProperty("_id");
    filter.setTarget(target);
    filter.setValueText(id);
    filter.setOperator(Operator.OPERATOR_EQUAL);
    req.setFilters(filter);

    if (!returnProperties.isEmpty() || !returnReferences.isEmpty()) {
      var properties = PropertiesRequest.newBuilder();
      for (String property : returnProperties) {
        properties.addNonRefProperties(property);
      }

      var references = RefPropertiesRequest.newBuilder();
      for (var ref : returnReferences) {
        ref.appendTo(references);
      }
      properties.addRefProperties(references);
      req.setProperties(properties);
    }
  }
}
