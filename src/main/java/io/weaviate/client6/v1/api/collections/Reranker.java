package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.api.collections.rerankers.CohereReranker;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JsonEnum;

public interface Reranker {
  public enum Kind implements JsonEnum<Kind> {
    COHERE("reranker-cohere");

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

  Kind _kind();

  Object _self();

  /** Configure a default Cohere reranker module. */
  public static Reranker cohere() {
    return CohereReranker.of();
  }

  /**
   * Configure a Cohere reranker module.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Reranker cohere(Function<CohereReranker.Builder, ObjectBuilder<CohereReranker>> fn) {
    return CohereReranker.of(fn);
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    private static final EnumMap<Reranker.Kind, TypeAdapter<? extends Reranker>> readAdapters = new EnumMap<>(
        Reranker.Kind.class);

    private final void addAdapter(Gson gson, Reranker.Kind kind, Class<? extends Reranker> cls) {
      readAdapters.put(kind, (TypeAdapter<? extends Reranker>) gson.getDelegateAdapter(this, TypeToken.get(cls)));
    }

    private final void init(Gson gson) {
      addAdapter(gson, Reranker.Kind.COHERE, CohereReranker.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      var rawType = type.getRawType();
      if (!Reranker.class.isAssignableFrom(rawType)) {
        return null;
      }

      if (readAdapters.isEmpty()) {
        init(gson);
      }

      final TypeAdapter<T> writeAdapter = (TypeAdapter<T>) gson.getDelegateAdapter(this, TypeToken.get(rawType));
      return (TypeAdapter<T>) new TypeAdapter<Reranker>() {

        @Override
        public void write(JsonWriter out, Reranker value) throws IOException {
          out.beginObject();
          out.name(value._kind().jsonValue());
          writeAdapter.write(out, (T) value._self());
          out.endObject();
        }

        @Override
        public Reranker read(JsonReader in) throws IOException {
          in.beginObject();
          var rerankerName = in.nextName();
          try {
            var kind = Reranker.Kind.valueOfJson(rerankerName);
            var adapter = readAdapters.get(kind);
            return adapter.read(in);
          } catch (IllegalArgumentException e) {
            return null;
          } finally {
            if (in.peek() == JsonToken.BEGIN_OBJECT) {
              in.beginObject();
            }
            in.endObject();
          }
        }
      }.nullSafe();
    }
  }
}
