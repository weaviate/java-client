package io.weaviate.client6.v1.api.collections.tenants;

import java.util.Collections;
import java.util.List;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record DeleteTenantsRequest(List<String> tenants) {
  static Endpoint<DeleteTenantsRequest, Void> endpoint(CollectionDescriptor<?> collection) {
    return SimpleEndpoint.sideEffect(
        __ -> "DELETE",
        __ -> "/schema/" + collection.collectionName() + "/tenants",
        __ -> Collections.emptyMap(),
        request -> JSON.serialize(request.tenants));
  }
}
