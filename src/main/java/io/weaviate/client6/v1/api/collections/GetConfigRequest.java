package io.weaviate.client6.v1.api.collections;

import java.util.Collections;
import java.util.Optional;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.OptionalEndpoint;

public record GetConfigRequest(String collectionName) {
  /**
   * Endpoint which deserializes the schema document into {@code cls}.
   *
   * <p>
   * Pass {@code String.class} to get the response body exactly as the server sent
   * it. {@link CollectionConfig} only carries what this client version models, so
   * the raw document is what you want when rendering or diffing a schema rather
   * than reading individual settings.
   */
  public static <T> Endpoint<GetConfigRequest, Optional<T>> endpoint(Class<T> cls) {
    return OptionalEndpoint.noBodyOptional(
        request -> "GET",
        request -> "/schema/" + request.collectionName,
        request -> Collections.emptyMap(),
        cls);
  }
}
