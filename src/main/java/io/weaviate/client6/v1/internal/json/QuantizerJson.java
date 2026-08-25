package io.weaviate.client6.v1.internal.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.api.collections.VectorIndex;

/**
 * Locating the quantizer inside a {@code vectorIndexConfig} payload.
 *
 * <p>
 * Shared by the read/write sides of {@code VectorConfig} and by the collection
 * update request, which all have to agree on where the quantizer keys live.
 */
public final class QuantizerJson {
  /** Prevent public initialization. */
  private QuantizerJson() {
  }

  /**
   * The object inside {@code vectorIndexConfig} which holds the quantizer keys.
   *
   * <p>
   * For {@code hnsw} and {@code flat} that is {@code vectorIndexConfig} itself,
   * but a {@code dynamic} index is
   * <code>{distance, threshold, hnsw: {...}, flat: {...}}</code> and each
   * sub-index carries its own quantizer, so the keys sit one level deeper.
   *
   * <p>
   * A vector config has a single quantization slot, so a dynamic index is read
   * from {@code hnsw} when it has a quantizer and from {@code flat} otherwise,
   * and written to {@code hnsw} -- which is also the only sub-index accepting
   * every quantizer type, {@code flat} being limited to {@code bq}. Giving
   * {@code hnsw} and {@code flat} <em>different</em> quantizers is not
   * expressible through this client.
   *
   * @param create when true the nested object is created if the payload does not
   *               have one yet; when false an empty object is returned instead,
   *               so the caller simply finds nothing.
   */
  public static JsonObject host(JsonObject vectorIndexConfig, JsonElement vectorIndexType, boolean create) {
    if (!isDynamic(vectorIndexType)) {
      return vectorIndexConfig;
    }

    for (var subIndex : new String[] { "hnsw", "flat" }) {
      var nested = vectorIndexConfig.get(subIndex);
      if (nested != null && nested.isJsonObject() && (create || hasQuantizer(nested.getAsJsonObject()))) {
        return nested.getAsJsonObject();
      }
    }

    if (create) {
      var hnsw = new JsonObject();
      vectorIndexConfig.add("hnsw", hnsw);
      return hnsw;
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
      if (subIndex.has(kind.jsonValue())) {
        return true;
      }
    }
    return false;
  }
}
