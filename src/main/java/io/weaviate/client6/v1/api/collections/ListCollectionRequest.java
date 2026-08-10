package io.weaviate.client6.v1.api.collections;

import java.util.Collections;

import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record ListCollectionRequest() {
  /**
   * Endpoint which deserializes the schema document into {@code cls}.
   *
   * <p>
   * Pass {@link ListCollectionResponse} to get the collections mapped onto
   * {@link CollectionConfig}, or {@code String.class} for the response body
   * exactly as the server sent it.
   *
   * @see GetConfigRequest#endpoint(Class)
   */
  public static <T> Endpoint<ListCollectionRequest, T> endpoint(Class<T> cls) {
    return SimpleEndpoint.noBody(
        request -> "GET",
        request -> "/schema",
        request -> Collections.emptyMap(),
        cls);
  }
}
