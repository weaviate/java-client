package io.weaviate.client6.v1.api.collections.data;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.XWriteWeaviateObject;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record InsertObjectRequest<PropertiesT>(XWriteWeaviateObject<PropertiesT> object) {

  @SuppressWarnings("unchecked")
  public static final <PropertiesT> Endpoint<InsertObjectRequest<PropertiesT>, XWriteWeaviateObject<PropertiesT>> endpoint(
      CollectionDescriptor<PropertiesT> collection,
      CollectionHandleDefaults defaults) {

    final var typeToken = (TypeToken<XWriteWeaviateObject<PropertiesT>>) TypeToken
        .getParameterized(XWriteWeaviateObject.class, collection.typeToken().getType());

    return new SimpleEndpoint<>(
        request -> "POST",
        request -> "/objects/",
        request -> defaults.consistencyLevel() != null
            ? Map.of("consistency_level", defaults.consistencyLevel())
            : Collections.emptyMap(),
        request -> JSON.serialize(
            new XWriteWeaviateObject<>(
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
      Function<XWriteWeaviateObject.Builder<PropertiesT>, ObjectBuilder<XWriteWeaviateObject<PropertiesT>>> fn) {
    return new InsertObjectRequest<>(XWriteWeaviateObject.of(ObjectBuilder.partial(fn, b -> b.properties(properties))));
  }
}
