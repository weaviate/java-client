package io.weaviate.client6.v1.api.collections.data;

import java.util.Collections;

import org.apache.hc.core5.http.HttpStatus;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;
import io.weaviate.client6.v1.internal.rest.Endpoint;

public record ReferenceDeleteRequest(String fromUuid, String fromProperty, Reference reference) {

  public static final Endpoint<ReferenceDeleteRequest, Void> endpoint(
      CollectionDescriptor<?> descriptor) {
    return Endpoint.of(
        request -> "DELETE",
        request -> "/objects/" + descriptor.name() + "/" + request.fromUuid + "/references/" + request.fromProperty,
        (gson, request) -> JSON.serialize(request.reference),
        request -> Collections.emptyMap(),
        code -> code != HttpStatus.SC_SUCCESS,
        (gson, response) -> null);
  }
}
