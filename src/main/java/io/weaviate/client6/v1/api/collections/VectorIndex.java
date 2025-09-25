package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.api.collections.vectorindex.Flat;
import io.weaviate.client6.v1.api.collections.vectorindex.Hnsw;
import io.weaviate.client6.v1.internal.json.JsonEnum;

public interface VectorIndex {
  static final String DEFAULT_VECTOR_NAME = "default";
  static final VectorIndex DEFAULT_VECTOR_INDEX = Hnsw.of();

  public enum Kind implements JsonEnum<Kind> {
    HNSW("hnsw"),
    FLAT("flat"),
    DYNAMIC("dynamic");

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

  VectorIndex.Kind _kind();

  /** Returns the on-the-wire name of the vector index type. */
  default String type() {
    return _kind().jsonValue();
  }

  /** Get the concrete vector index configuration object. */
  Object config();

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    private static final EnumMap<VectorIndex.Kind, TypeAdapter<? extends VectorIndex>> readAdapters = new EnumMap<>(
        VectorIndex.Kind.class);

    private final void addAdapter(Gson gson, VectorIndex.Kind kind, Class<? extends VectorIndex> cls) {
      readAdapters.put(kind, (TypeAdapter<? extends VectorIndex>) gson.getDelegateAdapter(this, TypeToken.get(cls)));
    }

    private final void init(Gson gson) {
      addAdapter(gson, VectorIndex.Kind.HNSW, Hnsw.class);
      addAdapter(gson, VectorIndex.Kind.FLAT, Flat.class);
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
          var config = writeAdapter.toJsonTree((T) value.config());
          config.getAsJsonObject().remove("name");
          Streams.write(config, out);

          out.endObject();
        }

        @Override
        public VectorIndex read(JsonReader in) throws IOException {
          var jsonObject = JsonParser.parseReader(in).getAsJsonObject();

          VectorIndex.Kind kind;
          var kindString = jsonObject.get("vectorIndexType").getAsString();
          try {
            kind = VectorIndex.Kind.valueOfJson(kindString);
          } catch (IllegalArgumentException e) {
            return null;
          }

          var adapter = readAdapters.get(kind);
          if (adapter == null) {
            return null;
          }

          var config = jsonObject.get("vectorIndexConfig").getAsJsonObject();
          return adapter.fromJsonTree(config);
        }
      }.nullSafe();
    }
  }
}
