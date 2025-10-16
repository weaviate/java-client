package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.api.collections.encoding.MuveraEncoding;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JsonEnum;

public interface Encoding {

  enum Kind implements JsonEnum<Kind> {
    MUVERA("muvera");

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

  public static Encoding muvera() {
    return MuveraEncoding.of();
  }

  public static Encoding muvera(Function<MuveraEncoding.Builder, ObjectBuilder<MuveraEncoding>> fn) {
    return MuveraEncoding.of(fn);
  }

  public enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    private static final EnumMap<Encoding.Kind, TypeAdapter<? extends Encoding>> delegateAdapters = new EnumMap<>(
        Encoding.Kind.class);

    private final void addAdapter(Gson gson, Encoding.Kind kind, Class<? extends Encoding> cls) {
      delegateAdapters.put(kind,
          (TypeAdapter<? extends Encoding>) gson.getDelegateAdapter(this, TypeToken.get(cls)));
    }

    private final void init(Gson gson) {
      addAdapter(gson, Encoding.Kind.MUVERA, MuveraEncoding.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      final var rawType = type.getRawType();
      if (!Encoding.class.isAssignableFrom(rawType)) {
        return null;
      }

      if (delegateAdapters.isEmpty()) {
        init(gson);
      }

      return (TypeAdapter<T>) new TypeAdapter<Encoding>() {

        @Override
        public void write(JsonWriter out, Encoding value) throws IOException {
          TypeAdapter<T> adapter = (TypeAdapter<T>) delegateAdapters.get(value._kind());
          adapter.write(out, (T) value._self());
        }

        @Override
        public Encoding read(JsonReader in) throws IOException {
          var encodingObject = JsonParser.parseReader(in).getAsJsonObject();
          var encodingName = encodingObject.keySet().iterator().next();

          Encoding.Kind kind;
          try {
            kind = Encoding.Kind.valueOfJson(encodingName);
          } catch (IllegalArgumentException e) {
            return null;
          }

          var adapter = delegateAdapters.get(kind);
          var concreteEncoding = encodingObject.get(encodingName).getAsJsonObject();
          return adapter.fromJsonTree(concreteEncoding);
        }
      }.nullSafe();
    }
  }
}
