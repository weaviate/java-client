package io.weaviate.client6.v1.api.collections.data;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record UpdateObjectRequest<PropertiesT>(WriteWeaviateObject<PropertiesT> object) {

  static final <PropertiesT> Endpoint<UpdateObjectRequest<PropertiesT>, Void> endpoint(
      CollectionDescriptor<PropertiesT> collection,
      CollectionHandleDefaults defaults) {

    final var typeToken = TypeToken.getParameterized(WriteWeaviateObject.class, collection.typeToken().getType());

    return SimpleEndpoint.sideEffect(
        request -> "PATCH",
        request -> "/objects/" + collection.collectionName() + "/" + request.object.uuid(),
        request -> defaults.consistencyLevel() != null
            ? Map.of("consistency_level", defaults.consistencyLevel())
            : Collections.emptyMap(),
        request -> JSON.serialize(
            new WriteWeaviateObject<>(
                request.object.uuid(),
                collection.collectionName(),
                defaults.tenant(),
                request.object.properties(),
                request.object.vectors(),
                request.object.createdAt(),
                request.object.lastUpdatedAt(),
                request.object.references()),
            typeToken));
  }

  public static <PropertiesT> UpdateObjectRequest<PropertiesT> of(String uuid,
      Function<UpdateObjectRequest.Builder<PropertiesT>, ObjectBuilder<UpdateObjectRequest<PropertiesT>>> fn) {
    return fn.apply(new Builder<>(uuid)).build();
  }

  public UpdateObjectRequest(Builder<PropertiesT> builder) {
    this(builder.build());
  }

  public static class Builder<PropertiesT> implements ObjectBuilder<WriteWeaviateObject<PropertiesT>> {
    private final WriteWeaviateObject.Builder<PropertiesT> object = new WriteWeaviateObject.Builder<>();

    public Builder(String uuid) {
      this.object.uuid(uuid);
    }

    public Builder<PropertiesT> properties(PropertiesT properties) {
      this.object.properties(properties);
      return this;
    }

    public Builder<PropertiesT> vectors(Vectors... vectors) {
      this.object.vectors(vectors);
      return this;
    }

    public Builder<PropertiesT> reference(String property, Reference... references) {
      this.object.reference(property, references);
      return this;
    }

    @Override
    public WriteWeaviateObject<PropertiesT> build() {
      return this.object.build();
    }
  }
}
