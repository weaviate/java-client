package io.weaviate.client6.v1.internal.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.api.collections.VectorIndex;

public final class QuantizerJson {
  private QuantizerJson() {
  }

  public static JsonObject host(JsonObject vectorIndexConfig, JsonElement vectorIndexType, boolean create) {
    if (!isDynamic(vectorIndexType)) {
      return vectorIndexConfig;
    }

    if (create) {
      var hnsw = vectorIndexConfig.get("hnsw");
      if (hnsw != null && hnsw.isJsonObject()) {
        return hnsw.getAsJsonObject();
      }
      var created = new JsonObject();
      vectorIndexConfig.add("hnsw", created);
      return created;
    }

    for (var subIndex : new String[] { "hnsw", "flat" }) {
      var nested = vectorIndexConfig.get(subIndex);
      if (nested != null && nested.isJsonObject() && hasQuantizer(nested.getAsJsonObject())) {
        return nested.getAsJsonObject();
      }
    }
    return new JsonObject();
  }

  private static boolean isDynamic(JsonElement vectorIndexType) {
    return vectorIndexType != null
        && vectorIndexType.isJsonPrimitive()
        && VectorIndex.Kind.DYNAMIC.jsonValue().equals(vectorIndexType.getAsString());
  }

  private static boolean hasQuantizer(JsonObject subIndex) {
    for (var kind : Quantization.Kind.values()) {
      var value = subIndex.get(kind.jsonValue());
      if (value == null) {
        continue;
      }
      if (kind == Quantization.Kind.UNCOMPRESSED) {
        if (isTrue(value)) {
          return true;
        }
      } else if (value.isJsonObject() && isTrue(value.getAsJsonObject().get("enabled"))) {
        return true;
      }
    }
    return false;
  }

  private static boolean isTrue(JsonElement value) {
    return value != null
        && value.isJsonPrimitive()
        && value.getAsJsonPrimitive().isBoolean()
        && value.getAsBoolean();
  }
}
