package io.weaviate.client6.v1.api.collections.data;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.BooleanEndpoint;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record DeleteObjectRequest(String uuid) {

  public static final Endpoint<DeleteObjectRequest, Boolean> endpoint(
      CollectionDescriptor<?> collection,
      CollectionHandleDefaults defaults) {
    return BooleanEndpoint.noBody(
        request -> "DELETE",
        request -> "/objects/" + collection.collectionName() + "/" + request.uuid,
        request -> defaults.queryParameters());
  }
}
