package io.weaviate.client6.v1.api.collections.data;

import java.util.Collections;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults.Location;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record DeleteObjectRequest(String uuid) {

  public static final Endpoint<DeleteObjectRequest, Void> endpoint(
      CollectionDescriptor<?> collection,
      CollectionHandleDefaults defaults) {
    return defaults.endpoint(
        SimpleEndpoint.sideEffect(
            request -> "DELETE",
            request -> "/objects/" + collection.name() + "/" + request.uuid,
            request -> Collections.emptyMap()),
        add -> add
            .consistencyLevel(Location.QUERY)
            .tenant(Location.QUERY));
  }
}
