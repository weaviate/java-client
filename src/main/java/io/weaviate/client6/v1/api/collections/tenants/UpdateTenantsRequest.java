package io.weaviate.client6.v1.api.collections.tenants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record UpdateTenantsRequest(List<Tenant> tenants) {
  /**
   * How many tenants the server accepts in a single update.
   *
   * <p>
   * {@code PUT /schema/{class}/tenants} rejects more than this with HTTP 422.
   * Note the asymmetry: adding tenants is not capped, only updating them, so
   * {@link CreateTenantsRequest} sends whatever it is given.
   */
  static final int MAX_TENANTS_PER_REQUEST = 100;

  /**
   * Split the tenants into requests the server will accept.
   *
   * <p>
   * The batches are views onto {@code tenants}, so the caller must not mutate it
   * while they are in flight.
   */
  static List<List<Tenant>> batches(List<Tenant> tenants) {
    if (tenants.size() <= MAX_TENANTS_PER_REQUEST) {
      return List.of(tenants);
    }

    var batches = new ArrayList<List<Tenant>>();
    for (int from = 0; from < tenants.size(); from += MAX_TENANTS_PER_REQUEST) {
      batches.add(tenants.subList(from, Math.min(from + MAX_TENANTS_PER_REQUEST, tenants.size())));
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
