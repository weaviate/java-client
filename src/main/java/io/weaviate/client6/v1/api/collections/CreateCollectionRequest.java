package io.weaviate.client6.v1.api.collections;

import java.util.Collections;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public record CreateCollectionRequest<T>(T collection) {
  public static <T> Endpoint<CreateCollectionRequest<T>, Void> endpoint() {
    return SimpleEndpoint.sideEffect(
        request -> "POST",
        request -> "/schema/",
        request -> Collections.emptyMap(),
        request -> request.collection instanceof String json
            ? json
            : JSON.serialize(request.collection));
  }

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
