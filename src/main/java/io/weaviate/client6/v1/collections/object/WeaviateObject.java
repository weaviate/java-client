package io.weaviate.client6.v1.collections.object;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public record WeaviateObject<T>(
    String collection,
    T properties,
    ObjectMetadata metadata) {

  public WeaviateObject(String collection, T properties, Consumer<ObjectMetadata.Builder> options) {
    this(collection, properties, ObjectMetadata.with(options));
  }

  // JSON serialization ----------------
  public static <T> WeaviateObject<T> fromJson(Gson gson, InputStream input) throws IOException {
    try (var r = new InputStreamReader(input)) {
      WeaviateObjectDTO<T> dto = gson.fromJson(r, new TypeToken<WeaviateObjectDTO<T>>() {
      }.getType());
      return dto.toWeaviateObject();
    }
  }

  public String toJson(Gson gson) {
    return gson.toJson(new WeaviateObjectDTO<>(this));
  }
}
