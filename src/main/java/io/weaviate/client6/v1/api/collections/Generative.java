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

import io.weaviate.client6.v1.api.collections.generative.CohereGenerative;
import io.weaviate.client6.v1.api.collections.generative.DummyGenerative;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JsonEnum;

public interface Generative {
  public enum Kind implements JsonEnum<Kind> {
    COHERE("generative-cohere"),
    DUMMY("generative-dummy");

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

  /** Configure a default Cohere generative module. */
  public static Generative cohere() {
    return CohereGenerative.of();
  }

  /**
   * Configure a Cohere generative module.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Generative cohere(Function<CohereGenerative.Builder, ObjectBuilder<CohereGenerative>> fn) {
    return CohereGenerative.of(fn);
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    private static final EnumMap<Generative.Kind, TypeAdapter<? extends Generative>> readAdapters = new EnumMap<>(
        Generative.Kind.class);

    private final void addAdapter(Gson gson, Generative.Kind kind, Class<? extends Generative> cls) {
      readAdapters.put(kind, (TypeAdapter<? extends Generative>) gson.getDelegateAdapter(this, TypeToken.get(cls)));
    }

    private final void init(Gson gson) {
      addAdapter(gson, Generative.Kind.COHERE, CohereGenerative.class);
      addAdapter(gson, Generative.Kind.DUMMY, DummyGenerative.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      var rawType = type.getRawType();
      if (!Generative.class.isAssignableFrom(rawType)) {
        return null;
      }

      if (readAdapters.isEmpty()) {
        init(gson);
      }

      final TypeAdapter<T> writeAdapter = (TypeAdapter<T>) gson.getDelegateAdapter(this, TypeToken.get(rawType));
      return (TypeAdapter<T>) new TypeAdapter<Generative>() {

        @Override
        public void write(JsonWriter out, Generative value) throws IOException {
          out.beginObject();
          out.name(value._kind().jsonValue());
          writeAdapter.write(out, (T) value._self());
          out.endObject();
        }

        @Override
        public Generative read(JsonReader in) throws IOException {
          in.beginObject();
          var moduleName = in.nextName();
          try {
            var kind = Generative.Kind.valueOfJson(moduleName);
            var adapter = readAdapters.get(kind);
            assert adapter != null : "no generative adapter for kind " + kind;
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
