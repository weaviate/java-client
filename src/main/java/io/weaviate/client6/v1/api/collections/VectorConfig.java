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
import io.weaviate.client6.v1.api.collections.vectorizers.SelfProvidedVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecContextionaryVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecWeaviateVectorizer;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.TaggedUnion;
import io.weaviate.client6.v1.internal.json.JsonEnum;

public interface VectorConfig extends TaggedUnion<VectorConfig.Kind, Object> {
  public enum Kind implements JsonEnum<Kind> {
    NONE("none"),
    IMG2VEC_NEURAL("img2vec-neural"),
    TEXT2VEC_CONTEXTIONARY("text2vec-contextionary"),
    TEXT2VEC_COHERE("text2vec-cohere"),
    TEXT2VEC_GOOGLE("text2vec-google"),
    TEXT2VEC_GOOGLEAISTUDIO("text2vec-google"),
    TEXT2VEC_HUGGINGFACE("text2vec-huggingface"),
    TEXT2VEC_MISTRAL("text2vec-mistral"),
    TEXT2VEC_MORPH("text2vec-morph"),
    TEXT2VEC_MODEL2VEC("text2vec-model2vec"),
    TEXT2VEC_OPENAI("text2vec-openai"),
    TEXT2VEC_OLLAMA("text2vec-ollama"),
    TEXT2VEC_TRANSFORMERS("text2vec-transformers"),
    TEXT2VEC_VOYAGEAI("text2vec-voyageai"),
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

  /** Get vector index configuration for this vector. */
  VectorIndex vectorIndex();

  /** Get quantization configuration for this vector. */
  Quantization quantization();

  /** Create a bring-your-own-vector vector index. */
  public static Map.Entry<String, VectorConfig> selfProvided() {
    return selfProvided(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a bring-your-own-vector vector index.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> selfProvided(
      Function<SelfProvidedVectorizer.Builder, ObjectBuilder<SelfProvidedVectorizer>> fn) {
    return selfProvided(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named bring-your-own-vector vector index.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> selfProvided(String vectorName) {
    return Map.entry(vectorName, SelfProvidedVectorizer.of());
  }

  /**
   * Create a named bring-your-own-vector vector index.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> selfProvided(String vectorName,
      Function<SelfProvidedVectorizer.Builder, ObjectBuilder<SelfProvidedVectorizer>> fn) {
    return Map.entry(vectorName, SelfProvidedVectorizer.of(fn));
  }

  /** Create a vector index with an {@code img2vec-neural} vectorizer. */
  public static Map.Entry<String, VectorConfig> img2vecNeural() {
    return img2vecNeural(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code img2vec-neural} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> img2vecNeural(
      Function<Img2VecNeuralVectorizer.Builder, ObjectBuilder<Img2VecNeuralVectorizer>> fn) {
    return img2vecNeural(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code img2vec-neural} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> img2vecNeural(String vectorName) {
    return Map.entry(vectorName, Img2VecNeuralVectorizer.of());
  }

  /**
   * Create a vector index with an {@code img2vec-neural} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> img2vecNeural(String vectorName,
      Function<Img2VecNeuralVectorizer.Builder, ObjectBuilder<Img2VecNeuralVectorizer>> fn) {
    return Map.entry(vectorName, Img2VecNeuralVectorizer.of(fn));
  }

  /** Create a vector index with an {@code multi2vec-clip} vectorizer. */
  public static Map.Entry<String, VectorConfig> multi2vecClip() {
    return multi2vecClip(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code multi2vec-clip} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecClip(
      Function<Multi2VecClipVectorizer.Builder, ObjectBuilder<Multi2VecClipVectorizer>> fn) {
    return multi2vecClip(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code multi2vec-clip} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> multi2vecClip(String vectorName) {
    return Map.entry(vectorName, Multi2VecClipVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code multi2vec-clip} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecClip(String vectorName,
      Function<Multi2VecClipVectorizer.Builder, ObjectBuilder<Multi2VecClipVectorizer>> fn) {
    return Map.entry(vectorName, Multi2VecClipVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-contextionary} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecContextionary() {
    return text2vecContextionary(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-contextionary} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecContextionary(
      Function<Text2VecContextionaryVectorizer.Builder, ObjectBuilder<Text2VecContextionaryVectorizer>> fn) {
    return text2vecContextionary(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-contextionary}
   * vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecContextionary(String vectorName) {
    return Map.entry(vectorName, Text2VecContextionaryVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-contextionary}
   * vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecContextionary(String vectorName,
      Function<Text2VecContextionaryVectorizer.Builder, ObjectBuilder<Text2VecContextionaryVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecContextionaryVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-weaviate} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2VecWeaviate() {
    return text2VecWeaviate(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-weaviate} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2VecWeaviate(
      Function<Text2VecWeaviateVectorizer.Builder, ObjectBuilder<Text2VecWeaviateVectorizer>> fn) {
    return text2VecWeaviate(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-weaviate} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2VecWeaviate(String vectorName) {
    return Map.entry(vectorName, Text2VecWeaviateVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-weaviate} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2VecWeaviate(String vectorName,
      Function<Text2VecWeaviateVectorizer.Builder, ObjectBuilder<Text2VecWeaviateVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecWeaviateVectorizer.of(fn));
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    private static final EnumMap<VectorConfig.Kind, TypeAdapter<? extends VectorConfig>> delegateAdapters = new EnumMap<>(
        VectorConfig.Kind.class);

    private final void addAdapter(Gson gson, VectorConfig.Kind kind, Class<? extends VectorConfig> cls) {
      delegateAdapters.put(kind,
          (TypeAdapter<? extends VectorConfig>) gson.getDelegateAdapter(this, TypeToken.get(cls)));
    }

    private final void init(Gson gson) {
      addAdapter(gson, VectorConfig.Kind.NONE, SelfProvidedVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.IMG2VEC_NEURAL, Img2VecNeuralVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.MULTI2VEC_CLIP, Multi2VecClipVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_WEAVIATE, Text2VecWeaviateVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_CONTEXTIONARY, Text2VecContextionaryVectorizer.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      final var rawType = type.getRawType();
      if (!VectorConfig.class.isAssignableFrom(rawType)) {
        return null;
      }

      if (delegateAdapters.isEmpty()) {
        init(gson);
      }

      return (TypeAdapter<T>) new TypeAdapter<VectorConfig>() {

        @Override
        public void write(JsonWriter out, VectorConfig value) throws IOException {
          TypeAdapter<T> adapter = (TypeAdapter<T>) delegateAdapters.get(value._kind());

          // Serialize vectorizer config as { "vectorizer-kind": { ... } }
          // and remove "vectorIndex" and quantization objects which every vectorizer has.
          var vectorizer = new JsonObject();
          var config = adapter.toJsonTree((T) value._self());

          // This will create { "vectorIndexType": "", "vectorIndexConfig": { ... } }
          // to which we just need to add "vectorizer": { ... } key
          // and "bq"/"pg"/"sq"/"rq": { ... } (quantizer) key.
          var vectorIndex = config.getAsJsonObject().remove("vectorIndex");

          vectorizer.add(value._kind().jsonValue(), config);
          vectorIndex.getAsJsonObject().add("vectorizer", vectorizer);

          if (value.quantization() != null && !config.getAsJsonObject().get("quantization").isJsonNull()) {
            vectorIndex.getAsJsonObject()
                .get("vectorIndexConfig").getAsJsonObject()
                .add(value.quantization()._kind().jsonValue(), config.getAsJsonObject().remove("quantization"));
          }

          Streams.write(vectorIndex, out);
        }

        @Override
        public VectorConfig read(JsonReader in) throws IOException {
          var jsonObject = JsonParser.parseReader(in).getAsJsonObject();
          var vectorIndexConfig = jsonObject.get("vectorIndexConfig").getAsJsonObject();

          String quantizationKind = null;
          if (vectorIndexConfig.has(Quantization.Kind.BQ.jsonValue())) {
            quantizationKind = Quantization.Kind.BQ.jsonValue();
          } else if (vectorIndexConfig.has(Quantization.Kind.PQ.jsonValue())) {
            quantizationKind = Quantization.Kind.PQ.jsonValue();
          } else if (vectorIndexConfig.has(Quantization.Kind.SQ.jsonValue())) {
            quantizationKind = Quantization.Kind.SQ.jsonValue();
          } else if (vectorIndexConfig.has(Quantization.Kind.RQ.jsonValue())) {
            quantizationKind = Quantization.Kind.RQ.jsonValue();
          } else {
            quantizationKind = Quantization.Kind.UNCOMPRESSED.jsonValue();
          }

          // VectorIndex.CustomTypeAdapterFactory expects keys
          // ["vectorIndexType", "vectorIndexConfig"].
          var vectorIndex = new JsonObject();
          vectorIndex.add("vectorIndexType", jsonObject.get("vectorIndexType"));
          vectorIndex.add("vectorIndexConfig", vectorIndexConfig);

          var vectorizerObject = jsonObject.get("vectorizer").getAsJsonObject();
          var vectorizerName = vectorizerObject.keySet().iterator().next();
          var concreteVectorizer = vectorizerObject.get(vectorizerName).getAsJsonObject();

          // Each individual vectorizer has a `VectorIndex vectorIndex` field.
          concreteVectorizer.add("vectorIndex", vectorIndex);

          VectorConfig.Kind kind;
          if (vectorizerName.equals(VectorConfig.Kind.TEXT2VEC_GOOGLE.jsonValue())) {
            kind = concreteVectorizer.has("projectId")
                ? VectorConfig.Kind.TEXT2VEC_GOOGLE
                : VectorConfig.Kind.TEXT2VEC_GOOGLEAISTUDIO;
          } else {
            try {
              kind = VectorConfig.Kind.valueOfJson(vectorizerName);
            } catch (IllegalArgumentException e) {
              return null;
            }
          }

          var adapter = delegateAdapters.get(kind);

          // Each individual vectorizer has a `Quantization quantization` field.
          // We need to specify the kind in order for
          // Quantization.CustomTypeAdapterFactory to be able to find the right adapter.
          if (vectorIndexConfig.has(quantizationKind)) {
            JsonObject quantization = new JsonObject();
            quantization.add(quantizationKind, vectorIndexConfig.get(quantizationKind));
            concreteVectorizer.add("quantization", quantization);
          } else {
            concreteVectorizer.add("quantization", null);
          }
          return adapter.fromJsonTree(concreteVectorizer);
        }
      }.nullSafe();
    }
  }
}
