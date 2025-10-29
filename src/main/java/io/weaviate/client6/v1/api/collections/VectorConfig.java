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
import io.weaviate.client6.v1.api.collections.vectorizers.Multi2MultiVecJinaAiVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Multi2VecAwsVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Multi2VecBindVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Multi2VecClipVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Multi2VecCohereVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Multi2VecGoogleVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Multi2VecJinaAiVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Multi2VecNvidiaVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Multi2VecVoyageAiVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Ref2VecCentroidVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.SelfProvidedVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2MultiVecJinaAiVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecAwsVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecAzureOpenAiVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecCohereVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecDatabricksVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecGoogleAiStudioVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecGoogleVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecHuggingFaceVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecJinaAiVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecMistralVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecModel2VecVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecMorphVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecNvidiaVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecOllamaVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecOpenAiVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecTransformersVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecVoyageAiVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecWeaviateVectorizer;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.TaggedUnion;
import io.weaviate.client6.v1.internal.json.JsonEnum;

public interface VectorConfig extends TaggedUnion<VectorConfig.Kind, Object> {
  public enum Kind implements JsonEnum<Kind> {
    NONE("none"),
    TEXT2VEC_AWS("text2vec-aws"),
    TEXT2VEC_COHERE("text2vec-cohere"),
    TEXT2VEC_DATABRICKS("text2vec-databricks"),
    TEXT2VEC_GOOGLE("text2vec-google"),
    TEXT2VEC_GOOGLEAISTUDIO("text2vec-google"),
    TEXT2VEC_HUGGINGFACE("text2vec-huggingface"),
    REF2VEC_CENTROID("ref2vec-centroid"),
    TEXT2VEC_JINAAI("text2vec-jinaai"),
    TEXT2VEC_MISTRAL("text2vec-mistral"),
    TEXT2VEC_MORPH("text2vec-morph"),
    TEXT2VEC_MODEL2VEC("text2vec-model2vec"),
    TEXT2VEC_NVIDIA("text2vec-nvidia"),
    TEXT2VEC_OPENAI("text2vec-openai"),
    TEXT2VEC_AZURE_OPENAI("text2vec-openai"),
    TEXT2VEC_OLLAMA("text2vec-ollama"),
    TEXT2VEC_TRANSFORMERS("text2vec-transformers"),
    TEXT2VEC_VOYAGEAI("text2vec-voyageai"),
    TEXT2VEC_WEAVIATE("text2vec-weaviate"),
    IMG2VEC_NEURAL("img2vec-neural"),
    MULTI2VEC_AWS("multi2vec-aws"),
    MULTI2VEC_BIND("multi2vec-bind"),
    MULTI2VEC_CLIP("multi2vec-clip"),
    MULTI2VEC_GOOGLE("multi2vec-google"),
    MULTI2VEC_COHERE("multi2vec-cohere"),
    MULTI2VEC_JINAAI("multi2vec-jinaai"),
    MULTI2VEC_NVIDIA("multi2vec-nvidia"),
    MULTI2VEC_VOYAGEAI("multi2vec-voyageai"),
    TEXT2MULTIVEC_JINAAI("text2multivec-jinaai"),
    MULTI2MULTIVEC_JINAAI("multi2multivec-jinaai");

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

  /** Create a vector index with an {@code multi2multivec-jinaai} vectorizer. */
  public static Map.Entry<String, VectorConfig> multi2multivecJinaai() {
    return multi2multivecJinaai(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code multi2multivec-jinaai} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2multivecJinaai(
      Function<Multi2MultiVecJinaAiVectorizer.Builder, ObjectBuilder<Multi2MultiVecJinaAiVectorizer>> fn) {
    return multi2multivecJinaai(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code multi2multivec-jinaai} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> multi2multivecJinaai(String vectorName) {
    return Map.entry(vectorName, Multi2MultiVecJinaAiVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code multi2multivec-jinaai} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2multivecJinaai(String vectorName,
      Function<Multi2MultiVecJinaAiVectorizer.Builder, ObjectBuilder<Multi2MultiVecJinaAiVectorizer>> fn) {
    return Map.entry(vectorName, Multi2MultiVecJinaAiVectorizer.of(fn));
  }

  /** Create a vector index with an {@code multi2vec-aws} vectorizer. */
  public static Map.Entry<String, VectorConfig> multi2vecAws() {
    return multi2vecAws(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code multi2vec-aws} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecAws(
      Function<Multi2VecAwsVectorizer.Builder, ObjectBuilder<Multi2VecAwsVectorizer>> fn) {
    return multi2vecAws(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code multi2vec-aws} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> multi2vecAws(String vectorName) {
    return Map.entry(vectorName, Multi2VecAwsVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code multi2vec-aws} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecAws(String vectorName,
      Function<Multi2VecAwsVectorizer.Builder, ObjectBuilder<Multi2VecAwsVectorizer>> fn) {
    return Map.entry(vectorName, Multi2VecAwsVectorizer.of(fn));
  }

  /** Create a vector index with an {@code multi2vec-bind} vectorizer. */
  public static Map.Entry<String, VectorConfig> multi2vecBind() {
    return multi2vecBind(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code multi2vec-bind} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecBind(
      Function<Multi2VecBindVectorizer.Builder, ObjectBuilder<Multi2VecBindVectorizer>> fn) {
    return multi2vecBind(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code multi2vec-bind} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> multi2vecBind(String vectorName) {
    return Map.entry(vectorName, Multi2VecBindVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code multi2vec-bind} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecBind(String vectorName,
      Function<Multi2VecBindVectorizer.Builder, ObjectBuilder<Multi2VecBindVectorizer>> fn) {
    return Map.entry(vectorName, Multi2VecBindVectorizer.of(fn));
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

  /** Create a vector index with an {@code multi2vec-cohere} vectorizer. */
  public static Map.Entry<String, VectorConfig> multi2vecCohere() {
    return multi2vecBind(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code multi2vec-cohere} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecCohere(
      Function<Multi2VecCohereVectorizer.Builder, ObjectBuilder<Multi2VecCohereVectorizer>> fn) {
    return multi2vecCohere(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code multi2vec-cohere} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> multi2vecCohere(String vectorName) {
    return Map.entry(vectorName, Multi2VecCohereVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code multi2vec-cohere} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecCohere(String vectorName,
      Function<Multi2VecCohereVectorizer.Builder, ObjectBuilder<Multi2VecCohereVectorizer>> fn) {
    return Map.entry(vectorName, Multi2VecCohereVectorizer.of(fn));
  }

  /**
   * Create a vector index with an {@code multi2vec-google} vectorizer.
   *
   * @param location Geographic region the Google Cloud model runs in.
   */
  public static Map.Entry<String, VectorConfig> multi2vecGoogle(String location) {
    return multi2vecGoogle(VectorIndex.DEFAULT_VECTOR_NAME, location);
  }

  /**
   * Create a vector index with an {@code multi2vec-google} vectorizer.
   *
   * @param location Geographic region the Google Cloud model runs in.
   * @param fn       Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecGoogle(
      String location,
      Function<Multi2VecGoogleVectorizer.Builder, ObjectBuilder<Multi2VecGoogleVectorizer>> fn) {
    return multi2vecGoogle(VectorIndex.DEFAULT_VECTOR_NAME, location, fn);
  }

  /**
   * Create a named vector index with an {@code multi2vec-google} vectorizer.
   *
   * @param vectorName Vector name.
   * @param location   Geographic region the Google Cloud model runs in.
   */
  public static Map.Entry<String, VectorConfig> multi2vecGoogle(String vectorName, String location) {
    return Map.entry(vectorName, Multi2VecGoogleVectorizer.of(location));
  }

  /**
   * Create a named vector index with an {@code multi2vec-google} vectorizer.
   *
   * @param vectorName Vector name.
   * @param location   Geographic region the Google Cloud model runs in.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecGoogle(String vectorName,
      String location,
      Function<Multi2VecGoogleVectorizer.Builder, ObjectBuilder<Multi2VecGoogleVectorizer>> fn) {
    return Map.entry(vectorName, Multi2VecGoogleVectorizer.of(location, fn));
  }

  /** Create a vector index with an {@code multi2vec-jinaai} vectorizer. */
  public static Map.Entry<String, VectorConfig> multi2vecJinaAi() {
    return multi2vecJinaAi(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code multi2vec-jinaai} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecJinaAi(
      Function<Multi2VecJinaAiVectorizer.Builder, ObjectBuilder<Multi2VecJinaAiVectorizer>> fn) {
    return multi2vecJinaAi(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code multi2vec-jinaai} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> multi2vecJinaAi(String vectorName) {
    return Map.entry(vectorName, Multi2VecJinaAiVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code multi2vec-jinaai} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecJinaAi(String vectorName,
      Function<Multi2VecJinaAiVectorizer.Builder, ObjectBuilder<Multi2VecJinaAiVectorizer>> fn) {
    return Map.entry(vectorName, Multi2VecJinaAiVectorizer.of(fn));
  }

  /** Create a vector index with an {@code multi2vec-nvidia} vectorizer. */
  public static Map.Entry<String, VectorConfig> multi2vecNvidia() {
    return multi2vecNvidia(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code multi2vec-nvidia} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecNvidia(
      Function<Multi2VecNvidiaVectorizer.Builder, ObjectBuilder<Multi2VecNvidiaVectorizer>> fn) {
    return multi2vecNvidia(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code multi2vec-nvidia} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> multi2vecNvidia(String vectorName) {
    return Map.entry(vectorName, Multi2VecNvidiaVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code multi2vec-nvidia} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecNvidia(String vectorName,
      Function<Multi2VecNvidiaVectorizer.Builder, ObjectBuilder<Multi2VecNvidiaVectorizer>> fn) {
    return Map.entry(vectorName, Multi2VecNvidiaVectorizer.of(fn));
  }

  /** Create a vector index with an {@code multi2vec-voyageai} vectorizer. */
  public static Map.Entry<String, VectorConfig> multi2vecVoyageAi() {
    return multi2vecVoyageAi(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code multi2vec-voyageai} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecVoyageAi(
      Function<Multi2VecVoyageAiVectorizer.Builder, ObjectBuilder<Multi2VecVoyageAiVectorizer>> fn) {
    return multi2vecVoyageAi(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code multi2vec-voyageai} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> multi2vecVoyageAi(String vectorName) {
    return Map.entry(vectorName, Multi2VecVoyageAiVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code multi2vec-voyageai} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> multi2vecVoyageAi(String vectorName,
      Function<Multi2VecVoyageAiVectorizer.Builder, ObjectBuilder<Multi2VecVoyageAiVectorizer>> fn) {
    return Map.entry(vectorName, Multi2VecVoyageAiVectorizer.of(fn));
  }

  /** Create a vector index with an {@code ref2vec-centroid} vectorizer. */
  public static Map.Entry<String, VectorConfig> ref2vecCentroid() {
    return ref2vecCentroid(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code ref2vec-centroid} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> ref2vecCentroid(
      Function<Ref2VecCentroidVectorizer.Builder, ObjectBuilder<Ref2VecCentroidVectorizer>> fn) {
    return ref2vecCentroid(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code ref2vec-centroid} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> ref2vecCentroid(String vectorName) {
    return Map.entry(vectorName, Ref2VecCentroidVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code ref2vec-centroid} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> ref2vecCentroid(String vectorName,
      Function<Ref2VecCentroidVectorizer.Builder, ObjectBuilder<Ref2VecCentroidVectorizer>> fn) {
    return Map.entry(vectorName, Ref2VecCentroidVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2multivec-jinaai} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2multivecJinaAi() {
    return text2multivecJinaAi(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2multivec-jinaai} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2multivecJinaAi(
      Function<Text2MultiVecJinaAiVectorizer.Builder, ObjectBuilder<Text2MultiVecJinaAiVectorizer>> fn) {
    return text2multivecJinaAi(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2multivec-jinaai} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2multivecJinaAi(String vectorName) {
    return Map.entry(vectorName, Text2MultiVecJinaAiVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2multivec-jinaai} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2multivecJinaAi(String vectorName,
      Function<Text2MultiVecJinaAiVectorizer.Builder, ObjectBuilder<Text2MultiVecJinaAiVectorizer>> fn) {
    return Map.entry(vectorName, Text2MultiVecJinaAiVectorizer.of(fn));
  }

  /**
   * Create a vector index with an {@code text2vec-aws} vectorizer with Bedrock
   * integration.
   *
   * @param model Inference model.
   */
  public static Map.Entry<String, VectorConfig> text2vecAwsBedrock(String model) {
    return text2vecAwsBedrock(VectorIndex.DEFAULT_VECTOR_NAME, model);
  }

  /**
   * Create a vector index with an {@code text2vec-aws} vectorizer with Bedrock
   * integration.
   *
   * @param model Inference model.
   * @param fn    Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecAwsBedrock(
      String model,
      Function<Text2VecAwsVectorizer.BedrockBuilder, ObjectBuilder<Text2VecAwsVectorizer>> fn) {
    return text2vecAwsBedrock(VectorIndex.DEFAULT_VECTOR_NAME, model, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-aws}
   * vectorizer with Bedrock integration.
   *
   * @param vectorName Vector name.
   * @param model      Inference model.
   */
  public static Map.Entry<String, VectorConfig> text2vecAwsBedrock(String vectorName, String model) {
    return Map.entry(vectorName, Text2VecAwsVectorizer.bedrock(model));
  }

  /**
   * Create a named vector index with an {@code text2vec-aws}
   * vectorizer with Bedrock integration.
   *
   * @param vectorName Vector name.
   * @param model      Inference model.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecAwsBedrock(String vectorName,
      String model,
      Function<Text2VecAwsVectorizer.BedrockBuilder, ObjectBuilder<Text2VecAwsVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecAwsVectorizer.bedrock(model, fn));
  }

  /**
   * Create a vector index with an {@code text2vec-aws} vectorizer with Sagemaker
   * integration.
   *
   * @param baseUrl Base URL of the inference service.
   */
  public static Map.Entry<String, VectorConfig> text2vecAwsSagemaker(String baseUrl) {
    return text2vecAwsSagemaker(VectorIndex.DEFAULT_VECTOR_NAME, baseUrl);
  }

  /**
   * Create a vector index with an {@code text2vec-aws} vectorizer with Sagemaker
   * integration.
   *
   * @param baseUrl Base URL of the inference service.
   * @param fn      Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecAwsSagemaker(
      String baseUrl,
      Function<Text2VecAwsVectorizer.SagemakerBuilder, ObjectBuilder<Text2VecAwsVectorizer>> fn) {
    return text2vecAwsSagemaker(VectorIndex.DEFAULT_VECTOR_NAME, baseUrl, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-aws}
   * vectorizer with Sagemaker integration.
   *
   * @param vectorName Vector name.
   * @param baseUrl    Base URL of the inference service.
   */
  public static Map.Entry<String, VectorConfig> text2vecAwsSagemaker(String vectorName, String baseUrl) {
    return Map.entry(vectorName, Text2VecAwsVectorizer.sagemaker(baseUrl));
  }

  /**
   * Create a named vector index with an {@code text2vec-aws}
   * vectorizer with Sagemaker integration.
   *
   * @param vectorName Vector name.
   * @param baseUrl    Base URL of the inference service.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecAwsSagemaker(String vectorName,
      String baseUrl,
      Function<Text2VecAwsVectorizer.SagemakerBuilder, ObjectBuilder<Text2VecAwsVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecAwsVectorizer.sagemaker(baseUrl, fn));
  }

  /**
   * Create a vector index with an {@code text2vec-openai} vectorizer deployed on
   * Azure.
   */
  public static Map.Entry<String, VectorConfig> text2vecAzureOpenAi() {
    return text2vecAzureOpenAi(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-openai} vectorizer deployed on
   * Azure.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecAzureOpenAi(
      Function<Text2VecAzureOpenAiVectorizer.Builder, ObjectBuilder<Text2VecAzureOpenAiVectorizer>> fn) {
    return text2vecAzureOpenAi(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-openai} vectorizer
   * deployed on Azure.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecAzureOpenAi(String vectorName) {
    return Map.entry(vectorName, Text2VecAzureOpenAiVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-openai} vectorizer
   * deployed on Azure.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecAzureOpenAi(String vectorName,
      Function<Text2VecAzureOpenAiVectorizer.Builder, ObjectBuilder<Text2VecAzureOpenAiVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecAzureOpenAiVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-cohere} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecCohere() {
    return text2vecCohere(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-cohere} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecCohere(
      Function<Text2VecCohereVectorizer.Builder, ObjectBuilder<Text2VecCohereVectorizer>> fn) {
    return text2vecCohere(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-cohere}
   * vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecCohere(String vectorName) {
    return Map.entry(vectorName, Text2VecCohereVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-cohere}
   * vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecCohere(String vectorName,
      Function<Text2VecCohereVectorizer.Builder, ObjectBuilder<Text2VecCohereVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecCohereVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-databricks} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecDatabricks() {
    return text2vecDatabricks(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-databricks} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecDatabricks(
      Function<Text2VecDatabricksVectorizer.Builder, ObjectBuilder<Text2VecDatabricksVectorizer>> fn) {
    return text2vecDatabricks(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-databricks} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecDatabricks(String vectorName) {
    return Map.entry(vectorName, Text2VecDatabricksVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-databricks} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecDatabricks(String vectorName,
      Function<Text2VecDatabricksVectorizer.Builder, ObjectBuilder<Text2VecDatabricksVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecDatabricksVectorizer.of(fn));
  }

  /**
   * Create a vector index with an {@code text2vec-google} vectorizer with Google
   * AI Studio integration.
   */
  public static Map.Entry<String, VectorConfig> text2vecGoogleAiStudio() {
    return text2vecGoogleAiStudio(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-google} vectorizer with Google
   * AI Studio integration.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecGoogleAiStudio(
      Function<Text2VecGoogleAiStudioVectorizer.Builder, ObjectBuilder<Text2VecGoogleAiStudioVectorizer>> fn) {
    return text2vecGoogleAiStudio(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-google} vectorizer with
   * Google AI Studio integration.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecGoogleAiStudio(String vectorName) {
    return Map.entry(vectorName, Text2VecGoogleAiStudioVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-google} vectorizer with
   * Google AI Studio integration.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecGoogleAiStudio(String vectorName,
      Function<Text2VecGoogleAiStudioVectorizer.Builder, ObjectBuilder<Text2VecGoogleAiStudioVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecGoogleAiStudioVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-google} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecGoogle() {
    return text2vecGoogle(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-google} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecGoogle(
      Function<Text2VecGoogleVectorizer.Builder, ObjectBuilder<Text2VecGoogleVectorizer>> fn) {
    return text2vecGoogle(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-google} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecGoogle(String vectorName) {
    return Map.entry(vectorName, Text2VecGoogleVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-google} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecGoogle(String vectorName,
      Function<Text2VecGoogleVectorizer.Builder, ObjectBuilder<Text2VecGoogleVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecGoogleVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-huggingface} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecHuggingFace() {
    return text2vecHuggingFace(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-huggingface} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecHuggingFace(
      Function<Text2VecHuggingFaceVectorizer.Builder, ObjectBuilder<Text2VecHuggingFaceVectorizer>> fn) {
    return text2vecHuggingFace(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-huggingface} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecHuggingFace(String vectorName) {
    return Map.entry(vectorName, Text2VecHuggingFaceVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-huggingface} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecHuggingFace(String vectorName,
      Function<Text2VecHuggingFaceVectorizer.Builder, ObjectBuilder<Text2VecHuggingFaceVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecHuggingFaceVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-jinaai} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecJinaAi() {
    return text2vecJinaAi(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-jinaai} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecJinaAi(
      Function<Text2VecJinaAiVectorizer.Builder, ObjectBuilder<Text2VecJinaAiVectorizer>> fn) {
    return text2vecJinaAi(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-jinaai} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecJinaAi(String vectorName) {
    return Map.entry(vectorName, Text2VecJinaAiVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-jinaai} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecJinaAi(String vectorName,
      Function<Text2VecJinaAiVectorizer.Builder, ObjectBuilder<Text2VecJinaAiVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecJinaAiVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-mistral} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecMistral() {
    return text2vecMistral(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-mistral} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecMistral(
      Function<Text2VecMistralVectorizer.Builder, ObjectBuilder<Text2VecMistralVectorizer>> fn) {
    return text2vecMistral(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-mistral} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecMistral(String vectorName) {
    return Map.entry(vectorName, Text2VecMistralVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-mistral} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecMistral(String vectorName,
      Function<Text2VecMistralVectorizer.Builder, ObjectBuilder<Text2VecMistralVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecMistralVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-model2vec} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecModel2Vec() {
    return text2vecModel2Vec(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-model2vec} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecModel2Vec(
      Function<Text2VecModel2VecVectorizer.Builder, ObjectBuilder<Text2VecModel2VecVectorizer>> fn) {
    return text2vecModel2Vec(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-model2vec} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecModel2Vec(String vectorName) {
    return Map.entry(vectorName, Text2VecModel2VecVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-model2vec} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecModel2Vec(String vectorName,
      Function<Text2VecModel2VecVectorizer.Builder, ObjectBuilder<Text2VecModel2VecVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecModel2VecVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-morph} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecMorph() {
    return text2vecMorph(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-morph} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecMorph(
      Function<Text2VecMorphVectorizer.Builder, ObjectBuilder<Text2VecMorphVectorizer>> fn) {
    return text2vecMorph(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-morph} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecMorph(String vectorName) {
    return Map.entry(vectorName, Text2VecMorphVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-morph} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecMorph(String vectorName,
      Function<Text2VecMorphVectorizer.Builder, ObjectBuilder<Text2VecMorphVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecMorphVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-nvidia} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecNvidia() {
    return text2vecNvidia(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-nvidia} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecNvidia(
      Function<Text2VecNvidiaVectorizer.Builder, ObjectBuilder<Text2VecNvidiaVectorizer>> fn) {
    return text2vecNvidia(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-nvidia} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecNvidia(String vectorName) {
    return Map.entry(vectorName, Text2VecNvidiaVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-nvidia} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecNvidia(String vectorName,
      Function<Text2VecNvidiaVectorizer.Builder, ObjectBuilder<Text2VecNvidiaVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecNvidiaVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-ollama} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecOllama() {
    return text2vecOllama(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-ollama} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecOllama(
      Function<Text2VecOllamaVectorizer.Builder, ObjectBuilder<Text2VecOllamaVectorizer>> fn) {
    return text2vecOllama(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-ollama} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecOllama(String vectorName) {
    return Map.entry(vectorName, Text2VecOllamaVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-ollama} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecOllama(String vectorName,
      Function<Text2VecOllamaVectorizer.Builder, ObjectBuilder<Text2VecOllamaVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecOllamaVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-openai} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecOpenAi() {
    return text2vecOpenAi(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-openai} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecOpenAi(
      Function<Text2VecOpenAiVectorizer.Builder, ObjectBuilder<Text2VecOpenAiVectorizer>> fn) {
    return text2vecOpenAi(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-openai} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecOpenAi(String vectorName) {
    return Map.entry(vectorName, Text2VecOpenAiVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-openai} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecOpenAi(String vectorName,
      Function<Text2VecOpenAiVectorizer.Builder, ObjectBuilder<Text2VecOpenAiVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecOpenAiVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-transformers} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecTransformers() {
    return text2vecTransformers(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-transformers} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecTransformers(
      Function<Text2VecTransformersVectorizer.Builder, ObjectBuilder<Text2VecTransformersVectorizer>> fn) {
    return text2vecTransformers(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-transformers} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecTransformers(String vectorName) {
    return Map.entry(vectorName, Text2VecTransformersVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-transformers} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecTransformers(String vectorName,
      Function<Text2VecTransformersVectorizer.Builder, ObjectBuilder<Text2VecTransformersVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecTransformersVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-voyageai} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecVoyageAi() {
    return text2vecVoyageAi(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-voyageai} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecVoyageAi(
      Function<Text2VecVoyageAiVectorizer.Builder, ObjectBuilder<Text2VecVoyageAiVectorizer>> fn) {
    return text2vecVoyageAi(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-voyageai} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecVoyageAi(String vectorName) {
    return Map.entry(vectorName, Text2VecVoyageAiVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-voyageai} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecVoyageAi(String vectorName,
      Function<Text2VecVoyageAiVectorizer.Builder, ObjectBuilder<Text2VecVoyageAiVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecVoyageAiVectorizer.of(fn));
  }

  /** Create a vector index with an {@code text2vec-weaviate} vectorizer. */
  public static Map.Entry<String, VectorConfig> text2vecWeaviate() {
    return text2vecWeaviate(VectorIndex.DEFAULT_VECTOR_NAME);
  }

  /**
   * Create a vector index with an {@code text2vec-weaviate} vectorizer.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecWeaviate(
      Function<Text2VecWeaviateVectorizer.Builder, ObjectBuilder<Text2VecWeaviateVectorizer>> fn) {
    return text2vecWeaviate(VectorIndex.DEFAULT_VECTOR_NAME, fn);
  }

  /**
   * Create a named vector index with an {@code text2vec-weaviate} vectorizer.
   *
   * @param vectorName Vector name.
   */
  public static Map.Entry<String, VectorConfig> text2vecWeaviate(String vectorName) {
    return Map.entry(vectorName, Text2VecWeaviateVectorizer.of());
  }

  /**
   * Create a named vector index with an {@code text2vec-weaviate} vectorizer.
   *
   * @param vectorName Vector name.
   * @param fn         Lambda expression for optional parameters.
   */
  public static Map.Entry<String, VectorConfig> text2vecWeaviate(String vectorName,
      Function<Text2VecWeaviateVectorizer.Builder, ObjectBuilder<Text2VecWeaviateVectorizer>> fn) {
    return Map.entry(vectorName, Text2VecWeaviateVectorizer.of(fn));
  }

  /** Is this an instance of {@link Img2VecNeuralVectorizer}? */
  default public boolean isImg2VecNeural() {
    return _is(VectorConfig.Kind.IMG2VEC_NEURAL);
  }

  /** Convert this instance to {@link Img2VecNeuralVectorizer}. */
  default public Img2VecNeuralVectorizer asImg2VecNeural() {
    return _as(VectorConfig.Kind.IMG2VEC_NEURAL);
  }

  /** Is this an instance of {@link Multi2MultiVecJinaAiVectorizer}? */
  default public boolean isMulti2MultiVecJinaAi() {
    return _is(VectorConfig.Kind.MULTI2MULTIVEC_JINAAI);
  }

  /** Convert this instance to {@link Multi2MultiVecJinaAiVectorizer}. */
  default public Multi2MultiVecJinaAiVectorizer asMulti2MultiVecJinaAi() {
    return _as(VectorConfig.Kind.MULTI2MULTIVEC_JINAAI);
  }

  /** Is this an instance of {@link Multi2VecAwsVectorizer}? */
  default public boolean isMulti2VecAws() {
    return _is(VectorConfig.Kind.MULTI2VEC_AWS);
  }

  /** Convert this instance to {@link Multi2VecAwsVectorizer}. */
  default public Multi2VecAwsVectorizer asMulti2VecAws() {
    return _as(VectorConfig.Kind.MULTI2VEC_AWS);
  }

  /** Is this an instance of {@link Multi2VecBindVectorizer}? */
  default public boolean isMulti2VecBind() {
    return _is(VectorConfig.Kind.MULTI2VEC_BIND);
  }

  /** Convert this instance to {@link Multi2VecBindVectorizer}. */
  default public Multi2VecBindVectorizer asMulti2VecBind() {
    return _as(VectorConfig.Kind.MULTI2VEC_BIND);
  }

  /** Is this an instance of {@link Multi2VecClipVectorizer}? */
  default public boolean isMulti2VecClip() {
    return _is(VectorConfig.Kind.MULTI2VEC_CLIP);
  }

  /** Convert this instance to {@link Multi2VecClipVectorizer}. */
  default public Multi2VecClipVectorizer asMulti2VecClip() {
    return _as(VectorConfig.Kind.MULTI2VEC_CLIP);
  }

  /** Is this an instance of {@link Multi2VecCohereVectorizer}? */
  default public boolean isMulti2VecCohere() {
    return _is(VectorConfig.Kind.MULTI2VEC_COHERE);
  }

  /** Convert this instance to {@link Multi2VecCohereVectorizer}. */
  default public Multi2VecCohereVectorizer asMulti2VecCohere() {
    return _as(VectorConfig.Kind.MULTI2VEC_COHERE);
  }

  /** Is this an instance of {@link Multi2VecGoogleVectorizer}? */
  default public boolean isMulti2VecGoogle() {
    return _is(VectorConfig.Kind.MULTI2VEC_GOOGLE);
  }

  /** Convert this instance to {@link Multi2VecGoogleVectorizer}. */
  default public Multi2VecGoogleVectorizer asMulti2VecGoogle() {
    return _as(VectorConfig.Kind.MULTI2VEC_GOOGLE);
  }

  /** Is this an instance of {@link Multi2VecJinaAiVectorizer}? */
  default public boolean isMulti2VecJinaAi() {
    return _is(VectorConfig.Kind.MULTI2VEC_JINAAI);
  }

  /** Convert this instance to {@link Multi2VecJinaAiVectorizer}. */
  default public Multi2VecJinaAiVectorizer asMulti2VecJinaAi() {
    return _as(VectorConfig.Kind.MULTI2VEC_JINAAI);
  }

  /** Is this an instance of {@link Multi2VecNvidiaVectorizer}? */
  default public boolean isMulti2VecNvidia() {
    return _is(VectorConfig.Kind.MULTI2VEC_NVIDIA);
  }

  /** Convert this instance to {@link Multi2VecNvidiaVectorizer}. */
  default public Multi2VecNvidiaVectorizer asMulti2VecNvidia() {
    return _as(VectorConfig.Kind.MULTI2VEC_NVIDIA);
  }

  /** Is this an instance of {@link Multi2VecVoyageAiVectorizer}? */
  default public boolean isMulti2VecVoyageAi() {
    return _is(VectorConfig.Kind.MULTI2VEC_VOYAGEAI);
  }

  /** Convert this instance to {@link Multi2VecVoyageAiVectorizer}. */
  default public Multi2VecVoyageAiVectorizer asMulti2VecVoyageAi() {
    return _as(VectorConfig.Kind.MULTI2VEC_VOYAGEAI);
  }

  /** Is this an instance of {@link Ref2VecCentroidVectorizer}? */
  default public boolean isRef2VecCentroid() {
    return _is(VectorConfig.Kind.REF2VEC_CENTROID);
  }

  /** Convert this instance to {@link Ref2VecCentroidVectorizer}. */
  default public Ref2VecCentroidVectorizer asRef2VecCentroid() {
    return _as(VectorConfig.Kind.REF2VEC_CENTROID);
  }

  /** Is this an instance of {@link Text2VecAwsVectorizer}? */
  default public boolean isText2VecAws() {
    return _is(VectorConfig.Kind.TEXT2VEC_AWS);
  }

  /** Convert this instance to {@link Text2VecAwsVectorizer}. */
  default public Text2VecAwsVectorizer asText2VecAws() {
    return _as(VectorConfig.Kind.TEXT2VEC_AWS);
  }

  /** Is this an instance of {@link Text2VecAzureOpenAiVectorizer}? */
  default public boolean isText2VecAzureOpenAi() {
    return _is(VectorConfig.Kind.TEXT2VEC_AZURE_OPENAI);
  }

  /** Convert this instance to {@link Text2VecAzureOpenAiVectorizer}. */
  default public Text2VecAzureOpenAiVectorizer asText2VecAzureOpenAi() {
    return _as(VectorConfig.Kind.TEXT2VEC_AZURE_OPENAI);
  }

  /** Is this an instance of {@link Text2VecCohereVectorizer}? */
  default public boolean isText2VecCohere() {
    return _is(VectorConfig.Kind.TEXT2VEC_COHERE);
  }

  /** Convert this instance to {@link Text2VecCohereVectorizer}. */
  default public Text2VecCohereVectorizer asText2VecCohere() {
    return _as(VectorConfig.Kind.TEXT2VEC_COHERE);
  }

  /** Is this an instance of {@link Text2VecDatabricksVectorizer}? */
  default public boolean isText2VecDatabricks() {
    return _is(VectorConfig.Kind.TEXT2VEC_DATABRICKS);
  }

  /** Convert this instance to {@link Text2VecDatabricksVectorizer}. */
  default public Text2VecDatabricksVectorizer asText2VecDatabricks() {
    return _as(VectorConfig.Kind.TEXT2VEC_DATABRICKS);
  }

  /** Is this an instance of {@link Text2VecGoogleAiStudioVectorizer}? */
  default public boolean isText2VecGoogleAiStudio() {
    return _is(VectorConfig.Kind.TEXT2VEC_GOOGLEAISTUDIO);
  }

  /** Convert this instance to {@link Text2VecGoogleAiStudioVectorizer}. */
  default public Text2VecGoogleAiStudioVectorizer asText2VecGoogleAiStudio() {
    return _as(VectorConfig.Kind.TEXT2VEC_GOOGLEAISTUDIO);
  }

  /** Is this an instance of {@link Text2VecGoogleVectorizer}? */
  default public boolean isText2VecGoogle() {
    return _is(VectorConfig.Kind.TEXT2VEC_GOOGLE);
  }

  /** Convert this instance to {@link Text2VecGoogleVectorizer}. */
  default public Text2VecGoogleVectorizer asText2VecGoogle() {
    return _as(VectorConfig.Kind.TEXT2VEC_GOOGLE);
  }

  /** Is this an instance of {@link Text2VecHuggingFaceVectorizer}? */
  default public boolean isText2VecHuggingFace() {
    return _is(VectorConfig.Kind.TEXT2VEC_HUGGINGFACE);
  }

  /** Convert this instance to {@link Text2VecHuggingFaceVectorizer}. */
  default public Text2VecHuggingFaceVectorizer asText2VecHuggingFace() {
    return _as(VectorConfig.Kind.TEXT2VEC_HUGGINGFACE);
  }

  /** Is this an instance of {@link Text2MultiVecJinaAiVectorizer}? */
  default public boolean isText2MultiVecJinaAi() {
    return _is(VectorConfig.Kind.TEXT2MULTIVEC_JINAAI);
  }

  /** Convert this instance to {@link Text2MultiVecJinaAiVectorizer}. */
  default public Text2MultiVecJinaAiVectorizer asText2MultiVecJinaAi() {
    return _as(VectorConfig.Kind.TEXT2MULTIVEC_JINAAI);
  }

  /** Is this an instance of {@link Text2VecJinaAiVectorizer}? */
  default public boolean isText2VecJinaAi() {
    return _is(VectorConfig.Kind.TEXT2VEC_JINAAI);
  }

  /** Convert this instance to {@link Text2VecJinaAiVectorizer}. */
  default public Text2VecJinaAiVectorizer asText2VecJinaAi() {
    return _as(VectorConfig.Kind.TEXT2VEC_JINAAI);
  }

  /** Is this an instance of {@link Text2VecMistralVectorizer}? */
  default public boolean isText2VecMistral() {
    return _is(VectorConfig.Kind.TEXT2VEC_MISTRAL);
  }

  /** Convert this instance to {@link Text2VecMistralVectorizer}. */
  default public Text2VecMistralVectorizer asText2VecMistral() {
    return _as(VectorConfig.Kind.TEXT2VEC_MISTRAL);
  }

  /** Is this an instance of {@link Text2VecModel2VecVectorizer}? */
  default public boolean isText2VecModel2Vec() {
    return _is(VectorConfig.Kind.TEXT2VEC_MODEL2VEC);
  }

  /** Convert this instance to {@link Text2VecModel2VecVectorizer}. */
  default public Text2VecModel2VecVectorizer asText2VecModel2Vec() {
    return _as(VectorConfig.Kind.TEXT2VEC_MODEL2VEC);
  }

  /** Is this an instance of {@link Text2VecMorphVectorizer}? */
  default public boolean isText2VecMorph() {
    return _is(VectorConfig.Kind.TEXT2VEC_MORPH);
  }

  /** Convert this instance to {@link Text2VecMorphVectorizer}. */
  default public Text2VecMorphVectorizer asText2VecMorph() {
    return _as(VectorConfig.Kind.TEXT2VEC_MORPH);
  }

  /** Is this an instance of {@link Text2VecNvidiaVectorizer}? */
  default public boolean isText2VecNvidia() {
    return _is(VectorConfig.Kind.TEXT2VEC_NVIDIA);
  }

  /** Convert this instance to {@link Text2VecNvidiaVectorizer}. */
  default public Text2VecNvidiaVectorizer asText2VecNvidia() {
    return _as(VectorConfig.Kind.TEXT2VEC_NVIDIA);
  }

  /** Is this an instance of {@link Text2VecOllamaVectorizer}? */
  default public boolean isText2VecOllama() {
    return _is(VectorConfig.Kind.TEXT2VEC_OLLAMA);
  }

  /** Convert this instance to {@link Text2VecOllamaVectorizer}. */
  default public Text2VecOllamaVectorizer asText2VecOllama() {
    return _as(VectorConfig.Kind.TEXT2VEC_OLLAMA);
  }

  /** Is this an instance of {@link Text2VecOpenAiVectorizer}? */
  default public boolean isText2VecOpenAi() {
    return _is(VectorConfig.Kind.TEXT2VEC_OPENAI);
  }

  /** Convert this instance to {@link Text2VecOpenAiVectorizer}. */
  default public Text2VecOpenAiVectorizer asText2VecOpenAi() {
    return _as(VectorConfig.Kind.TEXT2VEC_OPENAI);
  }

  /** Is this an instance of {@link Text2VecTransformersVectorizer}? */
  default public boolean isText2VecTransformers() {
    return _is(VectorConfig.Kind.TEXT2VEC_TRANSFORMERS);
  }

  /** Convert this instance to {@link Text2VecTransformersVectorizer}. */
  default public Text2VecTransformersVectorizer asText2VecTransformers() {
    return _as(VectorConfig.Kind.TEXT2VEC_TRANSFORMERS);
  }

  /** Is this an instance of {@link Text2VecVoyageAiVectorizer}? */
  default public boolean isText2VecVoyageAi() {
    return _is(VectorConfig.Kind.TEXT2VEC_VOYAGEAI);
  }

  /** Convert this instance to {@link Text2VecVoyageAiVectorizer}. */
  default public Text2VecVoyageAiVectorizer asText2VecVoyageAi() {
    return _as(VectorConfig.Kind.TEXT2VEC_VOYAGEAI);
  }

  /** Is this an instance of {@link Text2VecWeaviateVectorizer}? */
  default public boolean isText2VecWeaviate() {
    return _is(VectorConfig.Kind.TEXT2VEC_WEAVIATE);
  }

  /** Convert this instance to {@link Text2VecWeaviateVectorizer}. */
  default public Text2VecWeaviateVectorizer asText2VecWeaviate() {
    return _as(VectorConfig.Kind.TEXT2VEC_WEAVIATE);
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
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_AWS, Text2VecAwsVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_COHERE, Text2VecCohereVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_DATABRICKS, Text2VecDatabricksVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_GOOGLE, Text2VecGoogleVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_GOOGLEAISTUDIO, Text2VecGoogleAiStudioVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_HUGGINGFACE, Text2VecHuggingFaceVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.REF2VEC_CENTROID, Ref2VecCentroidVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_JINAAI, Text2VecJinaAiVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_MISTRAL, Text2VecMistralVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_MORPH, Text2VecMorphVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_MODEL2VEC, Text2VecModel2VecVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_NVIDIA, Text2VecNvidiaVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_OPENAI, Text2VecOpenAiVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_AZURE_OPENAI, Text2VecAzureOpenAiVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_OLLAMA, Text2VecOllamaVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_TRANSFORMERS, Text2VecTransformersVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_VOYAGEAI, Text2VecVoyageAiVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2VEC_WEAVIATE, Text2VecWeaviateVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.IMG2VEC_NEURAL, Img2VecNeuralVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.MULTI2VEC_AWS, Multi2VecAwsVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.MULTI2VEC_BIND, Multi2VecBindVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.MULTI2VEC_CLIP, Multi2VecClipVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.MULTI2VEC_GOOGLE, Multi2VecGoogleVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.MULTI2VEC_COHERE, Multi2VecCohereVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.MULTI2VEC_JINAAI, Multi2VecJinaAiVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.MULTI2VEC_NVIDIA, Multi2VecNvidiaVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.MULTI2VEC_VOYAGEAI, Multi2VecVoyageAiVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.TEXT2MULTIVEC_JINAAI, Text2MultiVecJinaAiVectorizer.class);
      addAdapter(gson, VectorConfig.Kind.MULTI2MULTIVEC_JINAAI, Multi2MultiVecJinaAiVectorizer.class);
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

          if (value.quantization() != null) {
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
          } else if (vectorizerName.equals(VectorConfig.Kind.TEXT2VEC_OPENAI.jsonValue())) {
            kind = concreteVectorizer.has("deployementId")
                ? VectorConfig.Kind.TEXT2VEC_AZURE_OPENAI
                : VectorConfig.Kind.TEXT2VEC_OPENAI;
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
