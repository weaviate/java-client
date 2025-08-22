package io.weaviate.client6.v1.api.collections.tenants;

import java.util.Collections;
import java.util.List;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record CreateTenantsRequest(List<Tenant> tenants) {
  static Endpoint<CreateTenantsRequest, Void> endpoint(CollectionDescriptor<?> collection) {
    return SimpleEndpoint.sideEffect(
        __ -> "POST",
        __ -> "/schema/" + collection.name() + "/tenants",
        __ -> Collections.emptyMap(),
        request -> JSON.serialize(request.tenants));
  }
}
