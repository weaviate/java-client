package io.weaviate.client6.v1.api.collections.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import io.weaviate.client6.v1.api.collections.query.Metadata.MetadataField;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;

public record QueryReference(
    String property,
    String collection,
    List<String> includeVectors,
    List<String> returnProperties,
    List<QueryReference> returnReferences,
    List<Metadata> returnMetadata) {

  public QueryReference(Builder options) {
    this(
        options.property,
        options.collection,
        new ArrayList<>(options.includeVectors),
        new ArrayList<>(options.returnProperties),
        options.returnReferences,
        new ArrayList<>(options.returnMetadata));
  }

  /**
   * Retrieve object referenced by a single-target cross-reference property.
   *
   * @param property Name of the cross-reference property.
   */
  public static QueryReference single(String property) {
    return single(property, ObjectBuilder.identity());
  }

  /**
   * Retrieve object referenced by a single-target cross-reference property.
   *
   * @param property Name of the cross-reference property.
   * @param fn       Lambda expression for optional parameters.
   */
  public static QueryReference single(String property, Function<Builder, ObjectBuilder<QueryReference>> fn) {
    return fn.apply(new Builder(null, property)).build();
  }

  // TODO: check if we can supply mutiple collections

  /**
   * Retrieve object referenced by a multi-target cross-reference property.
   *
   * @param property   Name of the cross-reference property.
   * @param collection Name of the target collection.
   */
  public static QueryReference multi(String property, String collection) {
    return multi(property, collection, ObjectBuilder.identity());
  }

  /**
   * Retrieve object referenced by a multi-target cross-reference property.
   *
   * @param property   Name of the cross-reference property.
   * @param collection Name of the target collection.
   * @param fn         Lambda expression for optional parameters.
   */
  public static QueryReference multi(String property, String collection,
      Function<Builder, ObjectBuilder<QueryReference>> fn) {
    return fn.apply(new Builder(collection, property)).build();
  }

  public static class Builder implements ObjectBuilder<QueryReference> {
    private final String property;
    private final String collection;

    private Set<String> includeVectors = new HashSet<>();
    private Set<String> returnProperties = new HashSet<>();
    private List<QueryReference> returnReferences = new ArrayList<>();
    private Set<Metadata> returnMetadata = new HashSet<>();

    public Builder(String collection, String property) {
      this.property = property;
      this.collection = collection;
      returnMetadata(MetadataField.UUID);
    }

    /** Select vectors to return for each referenced object. */
    public final Builder includeVectors(String... vectors) {
      this.includeVectors.addAll(Arrays.asList(vectors));
      return this;
    }

    /** Select properties to return for each referenced object. */
    public final Builder returnProperties(String... properties) {
      return returnProperties(Arrays.asList(properties));
    }

    /** Select properties to return for each referenced object. */
    public final Builder returnProperties(List<String> properties) {
      this.returnProperties.addAll(properties);
      return this;
    }

    /** Select nested references to return for each referenced object. */
    public final Builder returnReferences(QueryReference... references) {
      return returnReferences(Arrays.asList(references));
    }

    /** Select nested references to return for each referenced object. */
    public final Builder returnReferences(List<QueryReference> references) {
      this.returnReferences.addAll(references);
      return this;
    }

    /** Select metadata to return about each referenced object. */
    public final Builder returnMetadata(Metadata... metadata) {
      return returnMetadata(Arrays.asList(metadata));
    }

    /** Select metadata to return about each referenced object. */
    public final Builder returnMetadata(List<Metadata> metadata) {
      this.returnMetadata.addAll(metadata);
      return this;
    }

    /** Include the default vector of the referenced object. */
    public final Builder includeVector() {
      return returnMetadata(Metadata.VECTOR);
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
      metadata.addAllVectors(includeVectors);
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
