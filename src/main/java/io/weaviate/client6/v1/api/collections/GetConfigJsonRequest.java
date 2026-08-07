package io.weaviate.client6.v1.api.collections;

import java.util.Collections;
import java.util.Optional;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.OptionalEndpoint;

/**
 * Fetch a collection's schema as the server returned it, without mapping it
 * onto {@link CollectionConfig}.
 *
 * <p>
 * {@link GetConfigRequest} loses everything this client version does not model
 * &mdash; unknown module options, server-computed fields, and quantizer configs
 * nested inside a {@code dynamic} index. This request keeps the response body
 * intact, which is what you want when rendering or diffing a schema rather than
 * reading individual settings.
 */
public record GetConfigJsonRequest(String collectionName) {
  public static final Endpoint<GetConfigJsonRequest, Optional<String>> _ENDPOINT = OptionalEndpoint
      .noBodyOptional(
          request -> "GET",
          request -> "/schema/" + request.collectionName,
          request -> Collections.emptyMap(),
          (statusCode, response) -> response);
}
