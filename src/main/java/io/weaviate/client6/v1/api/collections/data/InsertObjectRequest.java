package io.weaviate.client6.v1.api.collections.data;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record InsertObjectRequest<PropertiesT>(WeaviateObject<PropertiesT> object) {

  @SuppressWarnings("unchecked")
  public static final <PropertiesT> Endpoint<InsertObjectRequest<PropertiesT>, WeaviateObject<PropertiesT>> endpoint(
      CollectionDescriptor<PropertiesT> collection,
      CollectionHandleDefaults defaults) {

    final var typeToken = (TypeToken<WeaviateObject<PropertiesT>>) TypeToken
        .getParameterized(WeaviateObject.class, collection.typeToken().getType());

    return new SimpleEndpoint<>(
        request -> "POST",
        request -> "/objects/",
        request -> defaults.consistencyLevel().isPresent()
            ? Map.of("consistency_level", defaults.consistencyLevel().get())
            : Collections.emptyMap(),
        request -> JSON.serialize(
            new WeaviateObject<>(
                request.object.uuid(),
                collection.collectionName(),
                defaults.tenant(),
                request.object.properties(),
                request.object.vectors(),
                request.object.createdAt(),
                request.object.lastUpdatedAt(),
                null, // no queryMetadata no insert
                request.object.references()),
            typeToken),
        (statusCode, response) -> JSON.deserialize(response, typeToken));
  }

  static <PropertiesT> InsertObjectRequest<PropertiesT> of(PropertiesT properties) {
    return of(properties, ObjectBuilder.identity());
  }

  static <PropertiesT> InsertObjectRequest<PropertiesT> of(
      PropertiesT properties,
      Function<WeaviateObject.Builder<PropertiesT>, ObjectBuilder<WeaviateObject<PropertiesT>>> fn) {
    return new InsertObjectRequest<>(WeaviateObject.of(ObjectBuilder.partial(fn, b -> b.properties(properties))));
  }
}
