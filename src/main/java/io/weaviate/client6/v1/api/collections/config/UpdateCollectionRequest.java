package io.weaviate.client6.v1.api.collections.config;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.WeaviateCollection;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record UpdateCollectionRequest(WeaviateCollection collection) {

  public static final Endpoint<UpdateCollectionRequest, Void> _ENDPOINT = Endpoint.of(
      request -> "PUT",
      request -> "/schema/" + request.collection.name(),
      (gson, request) -> JSON.serialize(request.collection),
      request -> Collections.emptyMap(),
      code -> code != HttpStatus.SC_SUCCESS,
      (gson, response) -> null);

  public static UpdateCollectionRequest of(String collectionName,
      Function<Builder, ObjectBuilder<UpdateCollectionRequest>> fn) {
    return fn.apply(new Builder(collectionName)).build();
  }

  public UpdateCollectionRequest(Builder builder) {
    this(builder.collection.build());
  }

  public static class Builder implements ObjectBuilder<UpdateCollectionRequest> {
    private final WeaviateCollection.Builder collection;

    public Builder(String collectionName) {
      this.collection = new WeaviateCollection.Builder(collectionName);
    }

    public Builder description(String description) {
      this.collection.description(description);
      return this;
    }

    public Builder vectors(Map.Entry<String, VectorIndex> vector) {
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
