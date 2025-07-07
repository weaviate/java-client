package io.weaviate.client6.v1.api.collections;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.weaviate.client6.v1.api.collections.vectorizers.Img2VecNeuralVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Multi2VecClipVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.NoneVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecContextionaryVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecWeaviateVectorizer;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JsonEnum;

public interface Vectorizer {
  public enum Kind implements JsonEnum<Kind> {
    NONE("none"),
    IMG2VEC_NEURAL("img2vec-neural"),
    TEXT2VEC_CONTEXTIONARY("text2vec-contextionary"),
    TEXT2VEC_WEAVIATE("text2vec-weaviate"),
    MULTI2VEC_CLIP("multi2vec-clip");

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

  VectorIndex vectorIndex();

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    private static final EnumMap<Vectorizer.Kind, TypeAdapter<? extends Vectorizer>> delegateAdapters = new EnumMap<>(
        Vectorizer.Kind.class);

    private final void addAdapter(Gson gson, Vectorizer.Kind kind, Class<? extends Vectorizer> cls) {
      delegateAdapters.put(kind, (TypeAdapter<? extends Vectorizer>) gson.getDelegateAdapter(this, TypeToken.get(cls)));
    }

    private final void init(Gson gson) {
      addAdapter(gson, Vectorizer.Kind.NONE, NoneVectorizer.class);
      addAdapter(gson, Vectorizer.Kind.IMG2VEC_NEURAL, Img2VecNeuralVectorizer.class);
      addAdapter(gson, Vectorizer.Kind.MULTI2VEC_CLIP, Multi2VecClipVectorizer.class);
      addAdapter(gson, Vectorizer.Kind.TEXT2VEC_WEAVIATE, Text2VecWeaviateVectorizer.class);
      addAdapter(gson, Vectorizer.Kind.TEXT2VEC_CONTEXTIONARY, Text2VecContextionaryVectorizer.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      final var rawType = type.getRawType();
      if (!Vectorizer.class.isAssignableFrom(rawType)) {
        return null;
      }

      if (delegateAdapters.isEmpty()) {
        init(gson);
      }

      return (TypeAdapter<T>) new TypeAdapter<Vectorizer>() {

        @Override
        public void write(JsonWriter out, Vectorizer value) throws IOException {
          TypeAdapter<T> adapter = (TypeAdapter<T>) delegateAdapters.get(value._kind());

          // Serialize vectorizer config as { "vectorizer-kind": { ... } }
          // and remove "vectorIndex" object which every vectorizer has.
          var vectorizer = new JsonObject();
          var config = adapter.toJsonTree((T) value._self());

          // This will create { "vectorIndexType": "", "vectorIndexConfig": { ... } }
          // to which we just need to add "vectorizer": { ... } key.
          var vectorIndex = config.getAsJsonObject().remove("vectorIndex");

          vectorizer.add(value._kind().jsonValue(), config);
          vectorIndex.getAsJsonObject().add("vectorizer", vectorizer);

          Streams.write(vectorIndex, out);
        }

        @Override
        public Vectorizer read(JsonReader in) throws IOException {
          var jsonObject = JsonParser.parseReader(in).getAsJsonObject();

          // VectorIndex.CustomTypeAdapterFactory expects keys
          // ["vectorIndexType", "vectorIndexConfig"].
          var vectorIndex = new JsonObject();
          vectorIndex.add("vectorIndexType", jsonObject.get("vectorIndexType"));
          vectorIndex.add("vectorIndexConfig", jsonObject.get("vectorIndexConfig"));

          var vectorizerObject = jsonObject.get("vectorizer").getAsJsonObject();
          var vectorizerName = vectorizerObject.keySet().iterator().next();

          Vectorizer.Kind kind;
          try {
            kind = Vectorizer.Kind.valueOfJson(vectorizerName);
          } catch (IllegalArgumentException e) {
            return null;
          }

          var adapter = delegateAdapters.get(kind);
          var concreteVectorizer = vectorizerObject.get(vectorizerName).getAsJsonObject();

          // Each individual vectorizer has a `VectorIndex vectorIndex` field.
          concreteVectorizer.add("vectorIndex", vectorIndex);

          return adapter.fromJsonTree(concreteVectorizer);
        }
      }.nullSafe();
    }
  }
}
