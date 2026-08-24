package io.weaviate.client6.v1.api.collections;

import java.util.Collections;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

/**
 * Create a collection from a {@link CollectionConfig} or from a raw JSON schema
 * definition.
 *
 * @param <T> Type of the payload. A {@code String} is sent verbatim, anything
 *            else is serialized first.
 */
public record CreateCollectionRequest<T>(T collection) {
  /**
   * Endpoint which sends the payload to {@code POST /schema}.
   *
   * <p>
   * A {@code String} payload is forwarded byte-for-byte, so it may use any
   * option the server accepts &mdash; including ones this client version does not
   * model. Nothing is validated client-side either: a typo in a key surfaces as a
   * server error rather than a compile error.
   *
   * <p>
   * The server echoes the stored configuration back, which this endpoint discards
   * &mdash; a raw payload may well describe a collection that
   * {@link CollectionConfig} cannot represent.
   */
  public static <T> Endpoint<CreateCollectionRequest<T>, Void> endpoint() {
    return SimpleEndpoint.sideEffect(
        request -> "POST",
        request -> "/schema/",
        request -> Collections.emptyMap(),
        request -> request.collection instanceof String json
            ? json
            : JSON.serialize(request.collection));
  }

  /**
   * Name of the collection defined by a raw JSON document (its {@code "class"}
   * key), which the client needs in order to return a handle for it.
   *
   * <p>
   * This is the only part of the document the client reads. Calling it before
   * sending doubles as validation: a document that is not usable as a
   * {@code POST /schema} payload fails before the request leaves the process
   * rather than after a round-trip.
   *
   * @throws IllegalArgumentException in case the string is not a JSON object or
   *                                  does not carry a {@code "class"} name.
   */
  public static String collectionNameFromJson(String json) {
    if (json == null || json.isBlank()) {
      throw new IllegalArgumentException("collection JSON must not be null or blank");
    }

    JsonElement document;
    try {
      document = JSON.toJsonElement(json);
    } catch (JsonParseException e) {
      throw new IllegalArgumentException("collection JSON is not valid JSON", e);
    }

    if (!document.isJsonObject()) {
      throw new IllegalArgumentException("collection JSON must be a JSON object");
    }

    var collectionName = document.getAsJsonObject().get("class");
    if (collectionName == null
        || !collectionName.isJsonPrimitive()
        || !collectionName.getAsJsonPrimitive().isString()
        || collectionName.getAsString().isBlank()) {
      throw new IllegalArgumentException(
          "collection JSON must have a non-empty string \"class\" key with the collection name");
    }
    return collectionName.getAsString();
  }
}
