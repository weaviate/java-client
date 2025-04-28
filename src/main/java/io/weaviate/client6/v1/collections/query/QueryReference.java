package io.weaviate.client6.v1.collections.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.MetadataRequest;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.PropertiesRequest;
import io.weaviate.client6.grpc.protocol.v1.WeaviateProtoSearchGet.RefPropertiesRequest;

public record QueryReference(
    String property,
    String collection,
    boolean includeVector, List<String> includeVectors,
    List<String> returnProperties,
    List<?> returnReferences,
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
    return single(property, opt -> {
    });
  }

  public static QueryReference single(String property, Consumer<Builder> fn) {
    var builder = new Builder(null, property);
    fn.accept(builder);
    return new QueryReference(builder);
  }

  // TODO: check if we can supply mutiple collections
  public static QueryReference multi(String property, String collection) {
    return multi(collection, property, opt -> {
    });
  }

  public static QueryReference multi(String property, String collection, Consumer<Builder> fn) {
    var builder = new Builder(collection, property);
    fn.accept(builder);
    return new QueryReference(builder);
  }

  public static QueryReference[] multi(String property, Consumer<Builder> fn, String... collections) {
    return Arrays.stream(collections).map(collection -> {
      var builder = new Builder(collection, property);
      fn.accept(builder);
      return new QueryReference(builder);
    }).toArray(QueryReference[]::new);
  }

  public static class Builder {
    private final String property;
    private final String collection;

    public Builder(String collection, String property) {
      this.property = property;
      this.collection = collection;
    }

    private boolean includeVector;
    private List<String> includeVectors = new ArrayList<>();
    private List<String> returnProperties = new ArrayList<>();
    private List<?> returnReferences = new ArrayList<>();
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

    public final Builder returnReferences(String... references) {
      this.returnReferences = Arrays.asList(references);
      return this;
    }

    public final Builder returnMetadata(Metadata... metadata) {
      this.returnMetadata = Arrays.asList(metadata);
      return this;
    }
  }

  public void appendTo(RefPropertiesRequest.Builder references) {
    references.setReferenceProperty(property);
    if (collection != null) {
      references.setTargetCollection(collection);
    }

    if (!returnMetadata.isEmpty()) {
      var metadata = MetadataRequest.newBuilder();
      returnMetadata.forEach(m -> m.appendTo(metadata));
      references.setMetadata(metadata);
    }

    if (!returnProperties.isEmpty()) {
      var properties = PropertiesRequest.newBuilder();
      for (String property : returnProperties) {
        properties.addNonRefProperties(property);
      }
      references.setProperties(properties);
    }
  }
}
