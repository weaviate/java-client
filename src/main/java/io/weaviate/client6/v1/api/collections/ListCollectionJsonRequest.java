package io.weaviate.client6.v1.api.collections;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

/**
 * Fetch the full schema as the server returned it, without mapping it onto
 * {@link CollectionConfig}.
 *
 * @see GetConfigJsonRequest
 */
public record ListCollectionJsonRequest() {
  public static final Endpoint<ListCollectionJsonRequest, String> _ENDPOINT = SimpleEndpoint.noBody(
      request -> "GET",
      request -> "/schema",
      request -> Collections.emptyMap(),
      (statusCode, response) -> response);
}
