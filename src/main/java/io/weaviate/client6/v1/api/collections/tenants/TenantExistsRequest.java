package io.weaviate.client6.v1.api.collections.tenants;

import java.util.Collections;

import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.BooleanEndpoint;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record TenantExistsRequest(String tenant) {
  static Endpoint<TenantExistsRequest, Boolean> endpoint(CollectionDescriptor<?> collection) {
    return new BooleanEndpoint<>(
        __ -> "GET",
        request -> "/schema/" + collection.name() + "/tenants/" + request.tenant,
        __ -> Collections.emptyMap(),
        __ -> null);
  }
}
