package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.api.collections.vectorindex.Dynamic;
import io.weaviate.client6.v1.api.collections.vectorindex.Flat;
import io.weaviate.client6.v1.api.collections.vectorindex.HFresh;
import io.weaviate.client6.v1.api.collections.vectorindex.Hnsw;
import io.weaviate.client6.v1.api.collections.vectorindex.None;
import io.weaviate.client6.v1.internal.TaggedUnion;
import io.weaviate.client6.v1.internal.json.JsonEnum;

public interface VectorIndex extends TaggedUnion<VectorIndex.Kind, Object> {
  static final String DEFAULT_VECTOR_NAME = "default";
  static final VectorIndex DEFAULT_VECTOR_INDEX = Hnsw.of();

  enum Kind implements JsonEnum<Kind> {
    HNSW("hnsw"),
    FLAT("flat"),
    DYNAMIC("dynamic"),
    HFRESH("hfresh"),
    NONE("none");

    private static final Map<String, Kind> jsonValueMap = JsonEnum.collectNames(Kind.values());
    private final String jsonValue;

    private Kind(String jsonValue) {
      this.jsonValue = jsonValue;
    }

    @Override
    public String jsonValue() {
      return this.jsonValue;
    }

    public static Kind valueOfJson(String jsonValue) {
      return JsonEnum.valueOfJson(jsonValue, jsonValueMap, Kind.class);
    }
  }

  /** Is this vector index of type HNSW? */
  default boolean isHnsw() {
    return _is(VectorIndex.Kind.HNSW);
  }

  /** Get as {@link Hnsw} instance. */
  default Hnsw asHnsw() {
    return _as(VectorIndex.Kind.HNSW);
  }

  /** Is this vector index of type FLAT? */
  default boolean isFlat() {
    return _is(VectorIndex.Kind.FLAT);
  }

  /** Get as {@link Flat} instance. */
  default Flat asFlat() {
    return _as(VectorIndex.Kind.FLAT);
  }

  /** Is this vector index of type DYNAMIC? */
  default boolean isDynamic() {
    return _is(VectorIndex.Kind.DYNAMIC);
  }

  /** Get as {@link Dynamic} instance. */
  default Dynamic asDynamic() {
    return _as(VectorIndex.Kind.DYNAMIC);
  }

  /** Is this vector index of type HFRESH? */
  default boolean isHFresh() {
    return _is(VectorIndex.Kind.HFRESH);
  }

  /** Get as {@link HFresh} instance. */
  default HFresh asHFresh() {
    return _as(VectorIndex.Kind.HFRESH);
  }

  /** Is this a "none" vector index? */
  default boolean isNone() {
    return _is(VectorIndex.Kind.NONE);
  }

  static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    private static final EnumMap<VectorIndex.Kind, TypeAdapter<? extends VectorIndex>> readAdapters = new EnumMap<>(
        VectorIndex.Kind.class);

    private final void addAdapter(Gson gson, VectorIndex.Kind kind, Class<? extends VectorIndex> cls) {
      readAdapters.put(kind, (TypeAdapter<? extends VectorIndex>) gson.getDelegateAdapter(this, TypeToken.get(cls)));
    }

    private final void init(Gson gson) {
      addAdapter(gson, VectorIndex.Kind.HNSW, Hnsw.class);
      addAdapter(gson, VectorIndex.Kind.FLAT, Flat.class);
      addAdapter(gson, VectorIndex.Kind.DYNAMIC, Dynamic.class);
      addAdapter(gson, VectorIndex.Kind.HFRESH, HFresh.class);
      addAdapter(gson, VectorIndex.Kind.NONE, None.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      var rawType = type.getRawType();
      if (!VectorIndex.class.isAssignableFrom(rawType)) {
        return null;
      }

      if (readAdapters.isEmpty()) {
        init(gson);
      }

      final var writeAdapter = gson.getDelegateAdapter(this, TypeToken.get(rawType));
      return (TypeAdapter<T>) new TypeAdapter<VectorIndex>() {

        @Override
        public void write(JsonWriter out, VectorIndex value) throws IOException {
          out.beginObject();
          out.name("vectorIndexType");
          out.value(value._kind().jsonValue());

          out.name("vectorIndexConfig");
          var config = writeAdapter.toJsonTree((T) value._self());
          config.getAsJsonObject().remove("name");
          Streams.write(config, out);

          out.endObject();
        }

        @Override
        public VectorIndex read(JsonReader in) throws IOException {
          var jsonObject = JsonParser.parseReader(in).getAsJsonObject();

          var vectorIndexType = jsonObject.get("vectorIndexType");
          if (vectorIndexType == null || vectorIndexType.isJsonNull()) {
            // VectorConfig.CustomTypeAdapterFactory cannot provide this
            // value for vector indexes that have been dropped.
            return null;
          }

          var vectorIndexConfig = jsonObject.get("vectorIndexConfig");
          if (vectorIndexConfig == null || vectorIndexConfig.isJsonNull()) {
            // VectorConfig.CustomTypeAdapterFactory cannot provide this
            // value for vector indexes that have been dropped.
            vectorIndexConfig = new JsonObject();
          }

          VectorIndex.Kind kind;
          var kindString = vectorIndexType.getAsString();
          try {
            kind = VectorIndex.Kind.valueOfJson(kindString);
          } catch (IllegalArgumentException e) {
            return null;
          }

          var adapter = readAdapters.get(kind);
          if (adapter == null) {
            return null;
          }

          var config = vectorIndexConfig.getAsJsonObject();
          return adapter.fromJsonTree(config);
        }
      }.nullSafe();
    }
  }
}
