package io.weaviate.client6.v1.api.collections.tenants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record UpdateTenantsRequest(List<Tenant> tenants) {
  static final int MAX_TENANTS_PER_REQUEST = 100;

  static List<List<Tenant>> batches(List<Tenant> tenants) {
    if (tenants.size() <= MAX_TENANTS_PER_REQUEST) {
      return List.of(List.copyOf(tenants));
    }

    var batches = new ArrayList<List<Tenant>>();
    for (int from = 0; from < tenants.size(); from += MAX_TENANTS_PER_REQUEST) {
      batches.add(List.copyOf(tenants.subList(from, Math.min(from + MAX_TENANTS_PER_REQUEST, tenants.size()))));
    }
    return batches;
  }

  static Endpoint<UpdateTenantsRequest, Void> endpoint(CollectionDescriptor<?> collection) {
    return SimpleEndpoint.sideEffect(
        __ -> "PUT",
        __ -> "/schema/" + collection.collectionName() + "/tenants",
        __ -> Collections.emptyMap(),
        request -> JSON.serialize(request.tenants));
  }
}
