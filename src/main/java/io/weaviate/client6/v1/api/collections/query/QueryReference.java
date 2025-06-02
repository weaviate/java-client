package io.weaviate.client6.v1.api.collections.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record QueryReference(
    String property,
    String collection,
    boolean includeVector,
    List<String> includeVectors,
    List<String> returnProperties,
    List<QueryReference> returnReferences,
    List<Metadata> returnMetadata) {

  public QueryReference(Builder options) {
    this(
        options.property,
        options.collection,
        options.includeVector,
        options.includeVectors,
        options.returnProperties,
        options.returnReferences,
        options.returnMetadata);
  }

  public static QueryReference single(String property) {
    return single(property, ObjectBuilder.identity());
  }

  public static QueryReference single(String property, Function<Builder, ObjectBuilder<QueryReference>> fn) {
    return fn.apply(new Builder(null, property)).build();
  }

  // TODO: check if we can supply mutiple collections
  public static QueryReference multi(String property, String collection) {
    return multi(collection, property, ObjectBuilder.identity());
  }

  public static QueryReference multi(String property, String collection,
      Function<Builder, ObjectBuilder<QueryReference>> fn) {
    return fn.apply(new Builder(collection, property)).build();
  }

  public static QueryReference[] multi(String property, Consumer<Builder> fn, String... collections) {
    return Arrays.stream(collections).map(collection -> {
      var builder = new Builder(collection, property);
      fn.accept(builder);
      return new QueryReference(builder);
    }).toArray(QueryReference[]::new);
  }

  public static class Builder implements ObjectBuilder<QueryReference> {
    private final String property;
    private final String collection;

    public Builder(String collection, String property) {
      this.property = property;
      this.collection = collection;
    }

    private boolean includeVector;
    private List<String> includeVectors = new ArrayList<>();
    private List<String> returnProperties = new ArrayList<>();
    private List<QueryReference> returnReferences = new ArrayList<>();
    private List<Metadata> returnMetadata = new ArrayList<>();

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

    public final Builder returnMetadata(Metadata... metadata) {
      this.returnMetadata = Arrays.asList(metadata);
      return this;
    }

    @Override
    public QueryReference build() {
      return new QueryReference(this);
    }
  }

  public void appendTo(WeaviateProtoSearchGet.RefPropertiesRequest.Builder references) {
    references.setReferenceProperty(property);
    if (collection != null) {
      references.setTargetCollection(collection);
    }

    if (!returnMetadata.isEmpty()) {
      var metadata = WeaviateProtoSearchGet.MetadataRequest.newBuilder();
      returnMetadata.forEach(m -> m.appendTo(metadata));
      references.setMetadata(metadata);
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
      references.setProperties(properties);
    }
  }
}
