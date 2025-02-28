package io.weaviate.client.v1.data.model;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;

import io.weaviate.client.v1.graphql.query.util.Serializer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeaviateObject {
  String id;
  @SerializedName("class")
  String className;
  Long creationTimeUnix;
  Long lastUpdateTimeUnix;
  Map<String, Object> properties;
  Map<String, Object> additional;

  /**
   * Unnamed (default) vector.
   * <p>
   * This field will be deprecated in later versions.
   * Prefer using {@link #vectors} for collections storing a single vector only.
   */
  Float[] vector;

  /** Named vectors. */
  @Builder.Default
  Map<String, Float[]> vectors = new HashMap<>();

  /** Named multivectors. */
  @Builder.Default
  transient Map<String, Float[][]> multiVectors = new HashMap<>();
  Object vectorWeights;
  String tenant;

  public static class Adapter implements JsonSerializer<WeaviateObject>, JsonDeserializer<WeaviateObject> {
    public static final Adapter INSTANCE = new Adapter();

    /**
     * This Gson instance does not have the {@link Adapter} registerred allowing us
     * to deserialize remaining {@link WeaviateObject} fields in a standard manner
     * without causing an infinite recursion.
     *
     * Mimicking {@link Serializer}, we disable HTML escaping to produce identical
     * results. Thankfully, its configuration is not too involved, so we can
     * tolerate this duplication. In the future, {@link Adapter} should be rewritten
     * to implement {@link TypeAdapterFactory}, so that the singleton instance can
     * be re-used in this context too.
     */
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Override
    public JsonElement serialize(WeaviateObject src, Type typeOfSrc, JsonSerializationContext ctx) {
      JsonObject result = gson.toJsonTree(src).getAsJsonObject();
      JsonObject vectors = result.getAsJsonObject("vectors");

      // Add multi-vectors to the named vectors map.
      for (Entry<String, Float[][]> entry : src.multiVectors.entrySet()) {
        String name = entry.getKey();
        JsonElement vector = gson.toJsonTree(entry.getValue());
        vectors.add(name, vector);
      }
      return result;
    }

    @Override
    public WeaviateObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
        throws JsonParseException {
      JsonObject jsonObject = json.getAsJsonObject();

      // Handle polymorphic "vectors" separately from the rest of the fields.
      Map<String, JsonElement> vectorsMap = new HashMap<>();
      if (jsonObject.has("vectors")) {
        vectorsMap = jsonObject.remove("vectors").getAsJsonObject().asMap();
      }
      WeaviateObject result = gson.fromJson(jsonObject, WeaviateObject.class);

      if (result.vectors == null) {
        result.vectors = new HashMap<>();
      }
      if (result.multiVectors == null) {
        result.multiVectors = new HashMap<>();
      }

      for (Entry<String, JsonElement> entry : vectorsMap.entrySet()) {
        String name = entry.getKey();
        JsonElement el = entry.getValue();
        if (el.isJsonArray()) {
          JsonArray array = el.getAsJsonArray();
          if (array.size() > 0 && array.get(0).isJsonArray()) {
            result.multiVectors.put(name, ctx.deserialize(array, Float[][].class));
          } else {
            result.vectors.put(name, ctx.deserialize(array, Float[].class));
          }
        }
      }

      return result;
    }
  }
}
