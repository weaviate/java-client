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

import io.weaviate.client6.v1.api.collections.generative.AnthropicGenerative;
import io.weaviate.client6.v1.api.collections.generative.AnyscaleGenerative;
import io.weaviate.client6.v1.api.collections.generative.AwsGenerative;
import io.weaviate.client6.v1.api.collections.generative.AzureOpenAiGenerative;
import io.weaviate.client6.v1.api.collections.generative.CohereGenerative;
import io.weaviate.client6.v1.api.collections.generative.DatabricksGenerative;
import io.weaviate.client6.v1.api.collections.generative.DummyGenerative;
import io.weaviate.client6.v1.api.collections.generative.FriendliaiGenerative;
import io.weaviate.client6.v1.api.collections.generative.GoogleGenerative;
import io.weaviate.client6.v1.api.collections.generative.MistralGenerative;
import io.weaviate.client6.v1.api.collections.generative.NvidiaGenerative;
import io.weaviate.client6.v1.api.collections.generative.OllamaGenerative;
import io.weaviate.client6.v1.api.collections.generative.OpenAiGenerative;
import io.weaviate.client6.v1.api.collections.generative.XaiGenerative;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.TaggedUnion;
import io.weaviate.client6.v1.internal.json.JsonEnum;

public interface Generative extends TaggedUnion<Generative.Kind, Object> {
  public enum Kind implements JsonEnum<Kind> {
    ANYSCALE("generative-anyscale"),
    AWS("generative-aws"),
    ANTHROPIC("generative-anthropic"),
    COHERE("generative-cohere"),
    DATABRICKS("generative-databricks"),
    FRIENDLIAI("generative-friendliai"),
    GOOGLE("generative-palm"),
    MISTRAL("generative-mistral"),
    NVIDIA("generative-nvidia"),
    OLLAMA("generative-ollama"),
    OPENAI("generative-openai"),
    AZURE_OPENAI("generative-openai"),
    XAI("generative-xai"),
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

  /** Configure a default {@code generative-anthropic} module. */
  public static Generative anthropic() {
    return AnthropicGenerative.of();
  }

  /**
   * Configure a {@code generative-anthropic} module.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Generative anthropic(Function<AnthropicGenerative.Builder, ObjectBuilder<AnthropicGenerative>> fn) {
    return AnthropicGenerative.of(fn);
  }

  /** Configure a default {@code generative-anyscale} module. */
  public static Generative anyscale() {
    return AnyscaleGenerative.of();
  }

  /**
   * Configure a {@code generative-anyscale} module.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Generative anyscale(Function<AnyscaleGenerative.Builder, ObjectBuilder<AnyscaleGenerative>> fn) {
    return AnyscaleGenerative.of(fn);
  }

  /**
   * Configure a default {@code generative-aws} module with Bedrock integration.
   *
   * @param region AWS region.
   * @param model  Model to use with Bedrock service.
   */
  public static Generative awsBedrock(String region, String model) {
    return AwsGenerative.bedrock(region, model);
  }

  /**
   * Configure a {@code generative-aws} module with Bedrock integration.
   *
   * @param region AWS region.
   * @param model  Model to use with Bedrock service.
   * @param fn     Lambda expression for optional parameters.
   */
  public static Generative awsBedrock(String region, String model,
      Function<AwsGenerative.BedrockBuilder, ObjectBuilder<AwsGenerative>> fn) {
    return AwsGenerative.bedrock(region, model, fn);
  }

  /**
   * Configure a default {@code generative-aws} module with Sagemaker integration.
   *
   * @param region  AWS region.
   * @param baseUrl Base inference URL.
   */
  public static Generative awsSagemaker(String region, String baseUrl) {
    return AwsGenerative.sagemaker(region, baseUrl);
  }

  /**
   * Configure a {@code generative-aws} module with Sagemaker integration.
   *
   * @param region  AWS region.
   * @param baseUrl Base inference URL.
   * @param fn      Lambda expression for optional parameters.
   */
  public static Generative awsSagemaker(String region, String baseUrl,
      Function<AwsGenerative.SagemakerBuilder, ObjectBuilder<AwsGenerative>> fn) {
    return AwsGenerative.sagemaker(region, baseUrl, fn);
  }

  /** Configure a default {@code generative-cohere} module. */
  public static Generative cohere() {
    return CohereGenerative.of();
  }

  /**
   * Configure a {@code generative-cohere} module.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Generative cohere(Function<CohereGenerative.Builder, ObjectBuilder<CohereGenerative>> fn) {
    return CohereGenerative.of(fn);
  }

  /**
   * Configure a default {@code generative-databricks} module.
   *
   * @param baseURL Base URL for the generative service.
   */
  public static Generative databricks(String baseURL) {
    return DatabricksGenerative.of(baseURL);
  }

  /**
   * Configure a {@code generative-databricks} module.
   *
   * @param baseURL Base URL for the generative service.
   * @param fn      Lambda expression for optional parameters.
   */
  public static Generative databricks(String baseURL,
      Function<DatabricksGenerative.Builder, ObjectBuilder<DatabricksGenerative>> fn) {
    return DatabricksGenerative.of(baseURL, fn);
  }

  /** Configure a default {@code generative-frienliai} module. */
  public static Generative frienliai() {
    return FriendliaiGenerative.of();
  }

  /**
   * Configure a {@code generative-frienliai} module.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Generative friendliai(Function<FriendliaiGenerative.Builder, ObjectBuilder<FriendliaiGenerative>> fn) {
    return FriendliaiGenerative.of(fn);
  }

  /** Configure a default {@code generative-palm} module. */
  public static Generative googleVertex(String projectId) {
    return GoogleGenerative.vertex(projectId);
  }

  /**
   * Configure a {@code generative-palm} module.
   *
   * @param projectId Project ID.
   * @param fn        Lambda expression for optional parameters.
   */
  public static Generative googleVertex(String projectId,
      Function<GoogleGenerative.VertexBuilder, ObjectBuilder<GoogleGenerative>> fn) {
    return GoogleGenerative.vertex(projectId, fn);
  }

  /** Configure a default {@code generative-palm} module. */
  public static Generative googleAiStudio() {
    return GoogleGenerative.aiStudio();
  }

  /**
   * Configure a {@code generative-palm} module.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Generative googleAiStudio(
      Function<GoogleGenerative.AiStudioBuilder, ObjectBuilder<GoogleGenerative>> fn) {
    return GoogleGenerative.aiStudio(fn);
  }

  /** Configure a default {@code generative-mistral} module. */
  public static Generative mistral() {
    return MistralGenerative.of();
  }

  /**
   * Configure a {@code generative-mistral} module.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Generative mistral(Function<MistralGenerative.Builder, ObjectBuilder<MistralGenerative>> fn) {
    return MistralGenerative.of(fn);
  }

  /** Configure a default {@code generative-nvidia} module. */
  public static Generative nvidia() {
    return NvidiaGenerative.of();
  }

  /**
   * Configure a {@code generative-nvidia} module.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Generative nvidia(Function<NvidiaGenerative.Builder, ObjectBuilder<NvidiaGenerative>> fn) {
    return NvidiaGenerative.of(fn);
  }

  /** Configure a default {@code generative-ollama} module. */
  public static Generative ollama() {
    return OllamaGenerative.of();
  }

  /**
   * Configure a {@code generative-ollama} module.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Generative ollama(Function<OllamaGenerative.Builder, ObjectBuilder<OllamaGenerative>> fn) {
    return OllamaGenerative.of(fn);
  }

  /** Configure a default {@code generative-openai} module. */
  public static Generative openai() {
    return OpenAiGenerative.of();
  }

  /**
   * Configure a {@code generative-openai} module.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Generative openai(Function<OpenAiGenerative.Builder, ObjectBuilder<OpenAiGenerative>> fn) {
    return OpenAiGenerative.of(fn);
  }

  /**
   * Configure a default {@code generative-openai} module
   * hosted on Microsoft Azure.
   *
   * @param resourceName Name of the Azure OpenAI resource.
   * @param deploymentId Azure OpenAI deployment ID.
   */
  public static Generative azure(String resourceName, String deploymentId) {
    return AzureOpenAiGenerative.of(resourceName, deploymentId);
  }

  /**
   * Configure a {@code generative-openai} module hosted on Microsoft Azure.
   *
   * @param resourceName Name of the Azure OpenAI resource.
   * @param deploymentId Azure OpenAI deployment ID.
   * @param fn           Lambda expression for optional parameters.
   */
  public static Generative azure(String resourceName, String deploymentId,
      Function<AzureOpenAiGenerative.Builder, ObjectBuilder<AzureOpenAiGenerative>> fn) {
    return AzureOpenAiGenerative.of(resourceName, deploymentId, fn);
  }

  /** Configure a default {@code generative-xai} module. */
  public static Generative xai() {
    return XaiGenerative.of();
  }

  /**
   * Configure a {@code generative-xai} module.
   *
   * @param fn Lambda expression for optional parameters.
   */
  public static Generative xai(Function<XaiGenerative.Builder, ObjectBuilder<XaiGenerative>> fn) {
    return XaiGenerative.of(fn);
  }

  /** Is this a {@code generative-anyscale} provider? */
  default boolean isAnyscale() {
    return _is(Generative.Kind.ANYSCALE);
  }

  /**
   * Get as {@link AnyscaleGenerative} instance.
   *
   * @throws IllegalStateException if the current kind is not
   *                               {@code generative-anyscale}.
   */
  default AnyscaleGenerative asAnyscale() {
    return _as(Generative.Kind.ANYSCALE);
  }

  /** Is this a {@code generative-aws} provider? */
  default boolean isAws() {
    return _is(Generative.Kind.AWS);
  }

  /**
   * Get as {@link AwsGenerative} instance.
   *
   * @throws IllegalStateException if the current kind is not
   *                               {@code generative-aws}.
   */
  default AwsGenerative asAws() {
    return _as(Generative.Kind.AWS);
  }

  /** Is this a {@code generative-anthropic} provider? */
  default boolean isAnthropic() {
    return _is(Generative.Kind.ANTHROPIC);
  }

  /**
   * Get as {@link AnthropicGenerative} instance.
   *
   * @throws IllegalStateException if the current kind is not
   *                               {@code generative-anthropic}.
   */
  default AnthropicGenerative asAnthropic() {
    return _as(Generative.Kind.ANTHROPIC);
  }

  /** Is this a {@code generative-cohere} provider? */
  default boolean isCohere() {
    return _is(Generative.Kind.COHERE);
  }

  /**
   * Get as {@link CohereGenerative} instance.
   *
   * @throws IllegalStateException if the current kind is not
   *                               {@code generative-cohere}.
   */
  default CohereGenerative asCohere() {
    return _as(Generative.Kind.COHERE);
  }

  /** Is this a {@code generative-databricks} provider? */
  default boolean isDatabricks() {
    return _is(Generative.Kind.DATABRICKS);
  }

  /**
   * Get as {@link DatabricksGenerative} instance.
   *
   * @throws IllegalStateException if the current kind is not
   *                               {@code generative-databricks}.
   */
  default DatabricksGenerative asDatabricks() {
    return _as(Generative.Kind.DATABRICKS);
  }

  /** Is this a {@code generative-friendliai} provider? */
  default boolean isFriendliai() {
    return _is(Generative.Kind.FRIENDLIAI);
  }

  /**
   * Get as {@link FriendliaiGenerative} instance.
   *
   * @throws IllegalStateException if the current kind is not
   *                               {@code generative-friendliai}.
   */
  default FriendliaiGenerative asFriendliai() {
    return _as(Generative.Kind.FRIENDLIAI);
  }

  /** Is this a {@code generative-palm} provider? */
  default boolean isGoogle() {
    return _is(Generative.Kind.GOOGLE);
  }

  /**
   * Get as {@link GoogleGenerative} instance.
   *
   * @throws IllegalStateException if the current kind is not
   *                               {@code generative-palm}.
   */
  default GoogleGenerative asGoogle() {
    return _as(Generative.Kind.GOOGLE);
  }

  /** Is this a {@code generative-mistral} provider? */
  default boolean isMistral() {
    return _is(Generative.Kind.MISTRAL);
  }

  /**
   * Get as {@link MistralGenerative} instance.
   *
   * @throws IllegalStateException if the current kind is not
   *                               {@code generative-mistral}.
   */
  default MistralGenerative asMistral() {
    return _as(Generative.Kind.MISTRAL);
  }

  /** Is this a {@code generative-nvidia} provider? */
  default boolean isNvidia() {
    return _is(Generative.Kind.NVIDIA);
  }

  /**
   * Get as {@link NvidiaGenerative} instance.
   *
   * @throws IllegalStateException if the current kind is not
   *                               {@code generative-nvidia}.
   */
  default NvidiaGenerative asNvidia() {
    return _as(Generative.Kind.NVIDIA);
  }

  /** Is this a {@code generative-ollama} provider? */
  default boolean isOllama() {
    return _is(Generative.Kind.OLLAMA);
  }

  /**
   * Get as {@link OllamaGenerative} instance.
   *
   * @throws IllegalStateException if the current kind is not
   *                               {@code generative-ollama}.
   */
  default OllamaGenerative asOllama() {
    return _as(Generative.Kind.OLLAMA);
  }

  /** Is this a {@code generative-openai} provider? */
  default boolean isOpenAi() {
    return _is(Generative.Kind.OPENAI);
  }

  /**
   * Get as {@link OpenAiGenerative} instance.
   *
   * @throws IllegalStateException if the current kind is not
   *                               {@code generative-openai}.
   */
  default OpenAiGenerative asOpenAi() {
    return _as(Generative.Kind.OPENAI);
  }

  /** Is this an Azure-specific {@code generative-openai} provider? */
  default boolean isAzure() {
    return _is(Generative.Kind.AZURE_OPENAI);
  }

  /**
   * Get as {@link AzureOpenAiGenerative} instance.
   *
   * @throws IllegalStateException if the current kind is not
   *                               {@code generative-openai}.
   */
  default AzureOpenAiGenerative asAzure() {
    return _as(Generative.Kind.AZURE_OPENAI);
  }

  /** Is this a {@code generative-xai} provider? */
  default boolean isXai() {
    return _is(Generative.Kind.XAI);
  }

  /**
   * Get as {@link XaiGenerative} instance.
   *
   * @throws IllegalStateException if the current kind is not
   *                               {@code generative-xai}.
   */
  default XaiGenerative asXai() {
    return _as(Generative.Kind.XAI);
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    private static final EnumMap<Generative.Kind, TypeAdapter<? extends Generative>> readAdapters = new EnumMap<>(
        Generative.Kind.class);

    private final void addAdapter(Gson gson, Generative.Kind kind, Class<? extends Generative> cls) {
      readAdapters.put(kind, (TypeAdapter<? extends Generative>) gson.getDelegateAdapter(this, TypeToken.get(cls)));
    }

    private final void init(Gson gson) {
      addAdapter(gson, Generative.Kind.ANYSCALE, AnyscaleGenerative.class);
      addAdapter(gson, Generative.Kind.ANTHROPIC, AnthropicGenerative.class);
      addAdapter(gson, Generative.Kind.AWS, AwsGenerative.class);
      addAdapter(gson, Generative.Kind.COHERE, CohereGenerative.class);
      addAdapter(gson, Generative.Kind.DATABRICKS, DatabricksGenerative.class);
      addAdapter(gson, Generative.Kind.GOOGLE, GoogleGenerative.class);
      addAdapter(gson, Generative.Kind.FRIENDLIAI, FriendliaiGenerative.class);
      addAdapter(gson, Generative.Kind.MISTRAL, MistralGenerative.class);
      addAdapter(gson, Generative.Kind.NVIDIA, NvidiaGenerative.class);
      addAdapter(gson, Generative.Kind.OLLAMA, OllamaGenerative.class);
      addAdapter(gson, Generative.Kind.OPENAI, OpenAiGenerative.class);
      addAdapter(gson, Generative.Kind.AZURE_OPENAI, AzureOpenAiGenerative.class);
      addAdapter(gson, Generative.Kind.XAI, XaiGenerative.class);
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

      final TypeAdapter<T> writeAdapter = (TypeAdapter<T>) gson.getDelegateAdapter(this,
          TypeToken.get(rawType));
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
          var jsonObject = JsonParser.parseReader(in).getAsJsonObject();
          var provider = jsonObject.keySet().iterator().next();

          var generative = jsonObject.get(provider).getAsJsonObject();
          Generative.Kind kind;
          if (provider.equals(Generative.Kind.OPENAI.jsonValue())) {
            kind = generative.has("deploymentId") && generative.has("resourceName")
                ? Generative.Kind.AZURE_OPENAI
                : Generative.Kind.OPENAI;
          } else {
            try {
              kind = Generative.Kind.valueOfJson(provider);
            } catch (IllegalArgumentException e) {
              return null;
            }
          }
          var adapter = readAdapters.get(kind);
          assert adapter != null : "no generative adapter for kind " + kind;
          return adapter.fromJsonTree(generative);
        }
      }.nullSafe();
    }
  }
}
