package io.weaviate.client6.v1.api.collections.data;

import java.util.Collections;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults.Location;
import io.weaviate.client6.v1.api.collections.ObjectMetadata;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record InsertObjectRequest<T>(WeaviateObject<T, Reference, ObjectMetadata> object) {

  @SuppressWarnings("unchecked")
  public static final <T> Endpoint<InsertObjectRequest<T>, WeaviateObject<T, Object, ObjectMetadata>> endpoint(
      CollectionDescriptor<T> collection,
      CollectionHandleDefaults defaults) {
    return defaults.endpoint(
        new SimpleEndpoint<>(
            request -> "POST",
            request -> "/objects/",
            request -> Collections.emptyMap(),
            request -> JSON.serialize(
                new WriteWeaviateObject<>(request.object, defaults.tenant()),
                TypeToken.getParameterized(
                    WriteWeaviateObject.class, collection.typeToken().getType())),
            (statusCode, response) -> JSON.deserialize(response,
                (TypeToken<WeaviateObject<T, Object, ObjectMetadata>>) TypeToken.getParameterized(
                    WeaviateObject.class, collection.typeToken().getType(), Object.class, ObjectMetadata.class))),
        add -> add
            .consistencyLevel(Location.QUERY));
  }

  public static <T> InsertObjectRequest<T> of(String collectionName, T properties) {
    return of(collectionName, properties, ObjectBuilder.identity());
  }

  public static <T> InsertObjectRequest<T> of(String collectionName, T properties,
      Function<Builder<T>, ObjectBuilder<InsertObjectRequest<T>>> fn) {
    return fn.apply(new Builder<T>(collectionName, properties)).build();
  }

  public InsertObjectRequest(Builder<T> builder) {
    this(builder.object.build());
  }

  public static class Builder<T> implements ObjectBuilder<InsertObjectRequest<T>> {
    private final WeaviateObject.Builder<T, Reference, ObjectMetadata> object = new WeaviateObject.Builder<>();
    private final ObjectMetadata.Builder metadata = new ObjectMetadata.Builder();

    public Builder(String collectionName, T properties) {
      this.object.collection(collectionName).properties(properties);
    }

    public Builder<T> uuid(String uuid) {
      this.metadata.uuid(uuid);
      return this;
    }

    public Builder<T> vectors(Vectors vectors) {
      this.metadata.vectors(vectors);
      return this;
    }

    public Builder<T> reference(String property, Reference... references) {
      this.object.reference(property, references);
      return this;
    }

    @Override
    public InsertObjectRequest<T> build() {
      this.object.metadata(this.metadata.build());
      return new InsertObjectRequest<>(this);
    }
  }

}
