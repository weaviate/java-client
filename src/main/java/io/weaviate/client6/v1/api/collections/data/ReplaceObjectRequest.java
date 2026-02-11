package io.weaviate.client6.v1.api.collections.data;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record ReplaceObjectRequest<PropertiesT>(WeaviateObject<PropertiesT> object) {

  static final <PropertiesT> Endpoint<ReplaceObjectRequest<PropertiesT>, Void> endpoint(
      CollectionDescriptor<PropertiesT> collection,
      CollectionHandleDefaults defaults) {

    final var typeToken = TypeToken.getParameterized(WeaviateObject.class, collection.typeToken().getType());

    return SimpleEndpoint.sideEffect(
        request -> "PUT",
        request -> "/objects/" + collection.collectionName() + "/" + request.object.uuid(),
        request -> defaults.consistencyLevel().isPresent()
            ? Map.of("consistency_level", defaults.consistencyLevel().get())
            : Collections.emptyMap(),
        request -> JSON.serialize(
            new WeaviateObject<>(
                request.object.uuid(),
                collection.collectionName(),
                defaults.tenant().get(),
                request.object.properties(),
                request.object.vectors(),
                request.object.createdAt(),
                request.object.lastUpdatedAt(),
                null,
                request.object.references()),
            typeToken));
  }

  public static <PropertiesT> ReplaceObjectRequest<PropertiesT> of(
      String uuid,
      Function<ReplaceObjectRequest.Builder<PropertiesT>, ObjectBuilder<ReplaceObjectRequest<PropertiesT>>> fn) {
    return fn.apply(new Builder<>(uuid)).build();
  }

  public ReplaceObjectRequest(Builder<PropertiesT> builder) {
    this(builder.object.build());
  }

  public static class Builder<PropertiesT> implements ObjectBuilder<ReplaceObjectRequest<PropertiesT>> {
    private final WeaviateObject.Builder<PropertiesT> object = new WeaviateObject.Builder<>();

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

    public Builder<PropertiesT> reference(String property, ObjectReference... references) {
      this.object.reference(property, references);
      return this;
    }

    @Override
    public ReplaceObjectRequest<PropertiesT> build() {
      return new ReplaceObjectRequest<>(this);
    }
  }
}
