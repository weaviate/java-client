package io.weaviate.client6.v1.api.collections;

import java.util.Collections;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

/**
 * Create a collection from a raw JSON schema definition.
 *
 * <p>
 * The JSON is sent to {@code POST /schema} verbatim: it is never mapped onto
 * {@link CollectionConfig}, so any configuration the server understands is
 * accepted, including options this client version does not model yet. The
 * flip side is that nothing is validated client-side either &mdash; a typo in a
 * key surfaces as a server error, not as a compile error.
 *
 * <p>
 * The only part of the document this client reads is the {@code "class"} key,
 * which is needed to return a handle for the created collection.
 */
public record CreateCollectionFromJsonRequest(String json) {
  public static final Endpoint<CreateCollectionFromJsonRequest, Void> _ENDPOINT = SimpleEndpoint.sideEffect(
      request -> "POST",
      request -> "/schema/",
      request -> Collections.emptyMap(),
      CreateCollectionFromJsonRequest::json);

  public CreateCollectionFromJsonRequest {
    // Fail before the request leaves the process rather than after a round-trip.
    parseCollectionName(json);
  }

  /** Name of the collection defined by this document ({@code "class"} key). */
  public String collectionName() {
    return parseCollectionName(json);
  }

  private static String parseCollectionName(String json) {
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
