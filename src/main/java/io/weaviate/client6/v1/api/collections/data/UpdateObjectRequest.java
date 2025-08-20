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

public record UpdateObjectRequest<T>(WeaviateObject<T, Reference, ObjectMetadata> object) {

  static final <T> Endpoint<UpdateObjectRequest<T>, Void> endpoint(CollectionDescriptor<T> collection,
      CollectionHandleDefaults defaults) {
    return defaults.endpoint(
        SimpleEndpoint.sideEffect(
            request -> "PATCH",
            request -> "/objects/" + collection.name() + "/" + request.object.metadata().uuid(),
            request -> Collections.emptyMap(),
            request -> JSON.serialize(
                new WriteWeaviateObject<>(request.object, defaults.tenant()),
                TypeToken.getParameterized(WriteWeaviateObject.class, collection.typeToken().getType()))),
        add -> add
            .consistencyLevel(Location.QUERY)
            .tenant(Location.BODY));
  }

  public static <T> UpdateObjectRequest<T> of(String collectionName, String uuid,
      Function<UpdateObjectRequest.Builder<T>, ObjectBuilder<UpdateObjectRequest<T>>> fn) {
    return fn.apply(new Builder<>(collectionName, uuid)).build();
  }

  public UpdateObjectRequest(Builder<T> builder) {
    this(builder.object.build());
  }

  public static class Builder<T> implements ObjectBuilder<UpdateObjectRequest<T>> {
    private final WeaviateObject.Builder<T, Reference, ObjectMetadata> object = new WeaviateObject.Builder<>();
    private final ObjectMetadata.Builder metadata = new ObjectMetadata.Builder();

    public Builder(String collectionName, String uuid) {
      this.object.collection(collectionName);
      this.metadata.uuid(uuid);
    }

    public Builder<T> properties(T properties) {
      this.object.properties(properties);
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
    public UpdateObjectRequest<T> build() {
      this.object.metadata(this.metadata.build());
      return new UpdateObjectRequest<>(this);
    }
  }
}
