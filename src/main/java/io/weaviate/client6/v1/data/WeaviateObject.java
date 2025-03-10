package io.weaviate.client6.v1.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

// TODO: unify this with collections.SearchObject

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class WeaviateObject<T> {
  public final String collection;
  public final T properties;
  public final Metadata metadata;

  @AllArgsConstructor
  public static class Metadata {
    public final String id;
    public final Vectors vectors;
  }

  WeaviateObject(String collection, T properties, Consumer<CustomMetadata> options) {
    var metadata = new CustomMetadata(options);

    this.collection = collection;
    this.properties = properties;
    this.metadata = new Metadata(metadata.id, metadata.vectors);
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
