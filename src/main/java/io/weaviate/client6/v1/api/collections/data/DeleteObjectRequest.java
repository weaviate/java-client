package io.weaviate.client6.v1.api.collections.data;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record DeleteObjectRequest(String uuid) {

  public static final Endpoint<DeleteObjectRequest, Void> endpoint(
      CollectionDescriptor<?> collection,
      CollectionHandleDefaults defaults) {
    return SimpleEndpoint.sideEffect(
        request -> "DELETE",
        request -> "/objects/" + collection.collectionName() + "/" + request.uuid,
        request -> defaults.queryParameters());
  }
}
