package io.weaviate.client6.v1.api.collections.config;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record UpdateCollectionRequest(CollectionConfig collection) {

  public static final Endpoint<UpdateCollectionRequest, Void> _ENDPOINT = Endpoint.of(
      request -> "PUT",
      request -> "/schema/" + request.collection.collectionName(),
      (gson, request) -> JSON.serialize(request.collection),
      request -> Collections.emptyMap(),
      code -> code != HttpStatus.SC_SUCCESS,
      (gson, response) -> null);

  public static UpdateCollectionRequest of(CollectionConfig collection,
      Function<Builder, ObjectBuilder<UpdateCollectionRequest>> fn) {
    return fn.apply(new Builder(collection)).build();
  }

  public UpdateCollectionRequest(Builder builder) {
    this(builder.newCollection.build());
  }

  public static class Builder implements ObjectBuilder<UpdateCollectionRequest> {
    private final CollectionConfig currentCollection;
    private final CollectionConfig.Builder newCollection;

    public Builder(CollectionConfig currentCollection) {
      this.currentCollection = currentCollection;
      this.newCollection = currentCollection.edit();
    }

    public Builder description(String description) {
      this.newCollection.description(description);
      return this;
    }

    @SafeVarargs
    public final Builder vectors(Map.Entry<String, VectorIndex>... vectors) {
      this.newCollection.vectors(Map.ofEntries(vectors));
      return this;
    }

    // TODO: propertyDescriptions
    // TODO: generative config
    // TODO: inverted index
    // TODO: replication
    // TODO: reranker

    @Override
    public UpdateCollectionRequest build() {
      return new UpdateCollectionRequest(this);
    }
  }
}
