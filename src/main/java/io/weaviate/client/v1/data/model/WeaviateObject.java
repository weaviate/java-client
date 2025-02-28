package io.weaviate.client.v1.data.model;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

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

  public static class Adapter implements JsonDeserializer<WeaviateObject> {
    public static final Adapter INSTANCE = new Adapter();

    /**
     * This Gson instance does not have the {@link Adapter} registerred allowing us
     * to deserialize remaining {@link WeaviateObject} fields in a standard manner
     * without causing an infinite recursion.
     */
    private static final Gson gson = new Gson();

    @Override
    public WeaviateObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
        throws JsonParseException {
      JsonObject jsonObject = json.getAsJsonObject();

      // Handle polymorphic "vectors" separately from the rest of the fields.
      JsonObject vectorsField = jsonObject.remove("vectors").getAsJsonObject();
      WeaviateObject result = gson.fromJson(jsonObject, WeaviateObject.class);

      if (result.vectors == null) {
        result.vectors = new HashMap<>();
      }
      if (result.multiVectors == null) {
        result.multiVectors = new HashMap<>();
      }

      vectorsField.asMap().entrySet().stream()
          .forEach(entry -> {
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
          });

      return result;
    }
  }
}
