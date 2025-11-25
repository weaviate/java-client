package io.weaviate.client6.v1.api.collections.data;

import java.util.List;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record ReferenceReplaceRequest(String fromUuid, String fromProperty, ObjectReference reference) {

  public static final Endpoint<ReferenceReplaceRequest, Void> endpoint(
      CollectionDescriptor<?> descriptor,
      CollectionHandleDefaults defaults) {
    return SimpleEndpoint.sideEffect(
        request -> "PUT",
        request -> "/objects/" + descriptor.collectionName() + "/" + request.fromUuid + "/references/"
            + request.fromProperty,
        request -> defaults.queryParameters(),
        request -> JSON.serialize(List.of(request.reference)));
  }
}
