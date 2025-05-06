package io.weaviate.client6.v1.collections.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBase.FilterTarget;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBase.Filters;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoBase.Filters.Operator;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataRequest;
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
    private List<String> includeVectors = new ArrayList<>();
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

    req.setFilters(Filters.newBuilder()
        .setTarget(FilterTarget.newBuilder().setProperty("_id"))
        .setValueText(id)
        .setOperator(Operator.OPERATOR_EQUAL));

    if (!returnProperties.isEmpty() || !returnReferences.isEmpty()) {
      var properties = PropertiesRequest.newBuilder();

      if (!returnProperties.isEmpty()) {
        properties.addAllNonRefProperties(returnProperties);
      }

      if (!returnReferences.isEmpty()) {
        returnReferences.forEach(r -> {
          var references = RefPropertiesRequest.newBuilder();
          r.appendTo(references);
          properties.addRefProperties(references);
        });
      }
      req.setProperties(properties);
    }

    // Always request UUID back in this request.
    var metadata = MetadataRequest.newBuilder().setUuid(true);
    if (includeVector) {
      metadata.setVector(true);
    } else if (!includeVectors.isEmpty()) {
      metadata.addAllVectors(includeVectors);
    }
    req.setMetadata(metadata);
  }
}
