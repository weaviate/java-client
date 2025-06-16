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

  public static Map.Entry<String, VectorIndex> none() {
    return Map.entry(VectorIndex.DEFAULT_VECTOR_NAME,
        VectorIndex.DEFAULT_VECTOR_INDEX.apply(new NoneVectorizer()));
  }

  public static Map.Entry<String, VectorIndex> none(String vectorName) {
    return Map.entry(vectorName,
        VectorIndex.DEFAULT_VECTOR_INDEX.apply(new NoneVectorizer()));
  }

  public static Map.Entry<String, VectorIndex> text2vecWeaviate() {
    return Map.entry(VectorIndex.DEFAULT_VECTOR_NAME,
        VectorIndex.DEFAULT_VECTOR_INDEX.apply(Text2VecWeaviateVectorizer.of()));
  }

  public static Map.Entry<String, VectorIndex> text2vecWeaviate(
      Function<Text2VecWeaviateVectorizer.Builder, ObjectBuilder<Text2VecWeaviateVectorizer>> fn) {
    return Map.entry(VectorIndex.DEFAULT_VECTOR_NAME,
        VectorIndex.DEFAULT_VECTOR_INDEX.apply(Text2VecWeaviateVectorizer.of(fn)));
  }

  public static Map.Entry<String, VectorIndex> text2vecWeaviate(String vectorName,
      Function<Text2VecWeaviateVectorizer.Builder, ObjectBuilder<Text2VecWeaviateVectorizer>> fn) {
    return Map.entry(vectorName,
        VectorIndex.DEFAULT_VECTOR_INDEX.apply(Text2VecWeaviateVectorizer.of(fn)));
  }

  public static Map.Entry<String, VectorIndex> text2VecContextionary() {
    return Map.entry(VectorIndex.DEFAULT_VECTOR_NAME,
        VectorIndex.DEFAULT_VECTOR_INDEX.apply(Text2VecContextionaryVectorizer.of()));
  }

  public static Map.Entry<String, VectorIndex> text2VecContextionary(
      Function<Text2VecContextionaryVectorizer.Builder, ObjectBuilder<Text2VecContextionaryVectorizer>> fn) {
    return Map.entry(VectorIndex.DEFAULT_VECTOR_NAME,
        VectorIndex.DEFAULT_VECTOR_INDEX.apply(Text2VecContextionaryVectorizer.of(fn)));
  }

  public static Map.Entry<String, VectorIndex> text2VecContextionary(String vectorName,
      Function<Text2VecContextionaryVectorizer.Builder, ObjectBuilder<Text2VecContextionaryVectorizer>> fn) {
    return Map.entry(vectorName,
        VectorIndex.DEFAULT_VECTOR_INDEX.apply(Text2VecContextionaryVectorizer.of(fn)));
  }

  public static Map.Entry<String, VectorIndex> multi2vecClip() {
    return Map.entry(VectorIndex.DEFAULT_VECTOR_NAME,
        VectorIndex.DEFAULT_VECTOR_INDEX.apply(Multi2VecClipVectorizer.of()));
  }

  public static Map.Entry<String, VectorIndex> multi2vecClip(
      Function<Multi2VecClipVectorizer.Builder, ObjectBuilder<Multi2VecClipVectorizer>> fn) {
    return Map.entry(VectorIndex.DEFAULT_VECTOR_NAME,
        VectorIndex.DEFAULT_VECTOR_INDEX.apply(Multi2VecClipVectorizer.of(fn)));
  }

  public static Map.Entry<String, VectorIndex> multi2vecClip(String vectorName,
      Function<Multi2VecClipVectorizer.Builder, ObjectBuilder<Multi2VecClipVectorizer>> fn) {
    return Map.entry(vectorName,
        VectorIndex.DEFAULT_VECTOR_INDEX.apply(Multi2VecClipVectorizer.of(fn)));
  }

  public static Map.Entry<String, VectorIndex> img2vecNeural() {
    return Map.entry(VectorIndex.DEFAULT_VECTOR_NAME,
        VectorIndex.DEFAULT_VECTOR_INDEX.apply(Img2VecNeuralVectorizer.of()));
  }

  public static Map.Entry<String, VectorIndex> img2vecNeural(
      Function<Img2VecNeuralVectorizer.Builder, ObjectBuilder<Img2VecNeuralVectorizer>> fn) {
    return Map.entry(VectorIndex.DEFAULT_VECTOR_NAME,
        VectorIndex.DEFAULT_VECTOR_INDEX.apply(Img2VecNeuralVectorizer.of(fn)));
  }

  public static Map.Entry<String, VectorIndex> img2vecNeural(String vectorName,
      Function<Img2VecNeuralVectorizer.Builder, ObjectBuilder<Img2VecNeuralVectorizer>> fn) {
    return Map.entry(vectorName,
        VectorIndex.DEFAULT_VECTOR_INDEX.apply(Img2VecNeuralVectorizer.of(fn)));
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    private static final EnumMap<Vectorizer.Kind, TypeAdapter<? extends Vectorizer>> readAdapters = new EnumMap<>(
        Vectorizer.Kind.class);

    private final void addAdapter(Gson gson, Vectorizer.Kind kind, Class<? extends Vectorizer> cls) {
      readAdapters.put(kind, (TypeAdapter<? extends Vectorizer>) gson.getDelegateAdapter(this, TypeToken.get(cls)));
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

      if (readAdapters.isEmpty()) {
        init(gson);
      }

      return (TypeAdapter<T>) new TypeAdapter<Vectorizer>() {

        @Override
        public void write(JsonWriter out, Vectorizer value) throws IOException {
          var writeAdapter = readAdapters.get(value._kind());

          out.beginObject();
          out.name(value._kind().jsonValue());
          ((TypeAdapter<T>) writeAdapter).write(out, (T) value._self());
          out.endObject();
        }

        @Override
        public Vectorizer read(JsonReader in) throws IOException {
          in.beginObject();
          var vectorizerName = in.nextName();
          try {
            var kind = Vectorizer.Kind.valueOfJson(vectorizerName);
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
