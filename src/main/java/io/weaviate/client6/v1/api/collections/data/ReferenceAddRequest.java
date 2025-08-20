package io.weaviate.client6.v1.api.collections.data;

import java.util.Collections;

import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults;
import io.weaviate.client6.v1.api.collections.CollectionHandleDefaults.Location;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record ReferenceAddRequest(String fromUuid, String fromProperty, Reference reference) {

  public static final Endpoint<ReferenceAddRequest, Void> endpoint(
      CollectionDescriptor<?> descriptor,
      CollectionHandleDefaults defautls) {
    return defautls.endpoint(
        SimpleEndpoint.sideEffect(
            request -> "POST",
            request -> "/objects/" + descriptor.name() + "/" + request.fromUuid + "/references/" + request.fromProperty,
            request -> Collections.emptyMap(),
            request -> JSON.serialize(request.reference)),
        add -> add
            .consistencyLevel(Location.QUERY));

  }
}
