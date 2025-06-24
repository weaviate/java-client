package io.weaviate.client6.v1.api.collections.data;

import java.util.Collections;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.api.collections.ObjectMetadata;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record UpdateObjectRequest<T>(WeaviateObject<T, Reference, ObjectMetadata> object) {

  static final <T> Endpoint<UpdateObjectRequest<T>, Void> endpoint(CollectionDescriptor<T> collectionDescriptor) {
    return Endpoint.of(
        request -> "PATCH",
        request -> "/objects/" + collectionDescriptor.name() + "/" + request.object.metadata().uuid(),
        (gson, request) -> JSON.serialize(request.object, TypeToken.getParameterized(
            WeaviateObject.class, collectionDescriptor.typeToken().getType(), Reference.class, ObjectMetadata.class)),
        request -> Collections.emptyMap(),
        code -> code != 204,
        (gson, response) -> null);
  }

  public static <T> UpdateObjectRequest<T> of(String uuid,
      Function<UpdateObjectRequest.Builder<T>, ObjectBuilder<UpdateObjectRequest<T>>> fn) {
    return fn.apply(new Builder<>(uuid)).build();
  }

  public UpdateObjectRequest(Builder<T> builder) {
    this(builder.object.build());
  }

  public static class Builder<T> implements ObjectBuilder<UpdateObjectRequest<T>> {
    private final WeaviateObject.Builder<T, Reference, ObjectMetadata> object = new WeaviateObject.Builder<>();
    private final ObjectMetadata.Builder metadata = new ObjectMetadata.Builder();

    public Builder(String uuid) {
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
