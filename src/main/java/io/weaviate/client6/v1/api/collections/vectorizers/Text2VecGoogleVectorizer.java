package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Text2VecGoogleVectorizer(
    @SerializedName("apiEndpoint") String baseUrl,
    @SerializedName("model") String model,
    @SerializedName("titleProperty") String titleProperty,
    @SerializedName("dimensions") Integer dimensions,
    @SerializedName("taskType") TaskType taskType,

    /** Google project ID. Only relevant for Vertex AI integration. */
    @SerializedName("projectId") String projectId,

    /**
     * Weaviate defaults to {@code true} if the value is not provided.
     * To avoid that we send "vectorizeClassName": false all the time
     * and make it impossible to enable this feature, as it is deprecated.
     */
    @Deprecated @SerializedName("vectorizeClassName") boolean vectorizeCollectionName,
    /** Properties included in the embedding. */
    @SerializedName("sourceProperties") List<String> sourceProperties,
    /** Vector index configuration. */
    VectorIndex vectorIndex,
    /** Vector quantization method. */
    Quantization quantization) implements VectorConfig {

  public enum TaskType {
    @SerializedName("RETRIEVAL_QUERY")
    RETRIEVAL_QUERY,
    @SerializedName("CODE_RETRIEVAL_QUERY")
    CODE_RETRIEVAL_QUERY,
    @SerializedName("QUESTION_ANSWERING")
    QUESTION_ANSWERING,
    @SerializedName("FACT_VERIFICATION")
    FACT_VERIFICATION,
    @SerializedName("CLASSIFICATION")
    CLASSIFICATION,
    @SerializedName("CLUSTERING")
    CLUSTERING,
    @SerializedName("SEMANTIC_SIMILARITY")
    SEMANTIC_SIMILARITY;
  }

  @Override
  public VectorConfig.Kind _kind() {
    return VectorConfig.Kind.TEXT2VEC_GOOGLE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Text2VecGoogleVectorizer aiStudio() {
    return aiStudio(ObjectBuilder.identity());
  }

  public static Text2VecGoogleVectorizer aiStudio(
      Function<AiStudioBuilder, ObjectBuilder<Text2VecGoogleVectorizer>> fn) {
    return fn.apply(new AiStudioBuilder()).build();
  }

  public static Text2VecGoogleVectorizer vertex(String projectId) {
    return vertex(projectId, ObjectBuilder.identity());
  }

  public static Text2VecGoogleVectorizer vertex(
      String projectId,
      Function<VertexBuilder, ObjectBuilder<Text2VecGoogleVectorizer>> fn) {
    return fn.apply(new VertexBuilder(projectId)).build();
  }

  /**
   * Canonical constructor always sets {@link #vectorizeCollectionName} to false.
   */
  public Text2VecGoogleVectorizer(
      String baseUrl,
      String model,
      String titleProperty,
      Integer dimensions,
      TaskType taskType,
      String projectId,

      boolean vectorizeCollectionName,
      List<String> sourceProperties,
      VectorIndex vectorIndex,
      Quantization quantization) {
    this.baseUrl = baseUrl;
    this.model = model;
    this.titleProperty = titleProperty;
    this.dimensions = dimensions;
    this.projectId = projectId;
    this.taskType = taskType;

    this.vectorizeCollectionName = false;
    this.sourceProperties = sourceProperties;
    this.vectorIndex = vectorIndex;
    this.quantization = quantization;
  }

  public Text2VecGoogleVectorizer(Builder builder) {
    this(
        builder.baseUrl,
        builder.model,
        builder.titleProperty,
        builder.dimensions,
        builder.taskType,
        builder.projectId,

        builder.vectorizeCollectionName,
        builder.sourceProperties,
        builder.vectorIndex,
        builder.quantization);
  }

  public abstract static class Builder implements ObjectBuilder<Text2VecGoogleVectorizer> {
    private final boolean vectorizeCollectionName = false;
    private Quantization quantization;
    private List<String> sourceProperties = new ArrayList<>();
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;

    /** Embedding service base URL. */
    private String baseUrl;
    /** Google project ID. Only relevant for Vertex AI integration. */
    private final String projectId;

    private String model;
    private String titleProperty;
    private Integer dimensions;
    private TaskType taskType;

    public Builder(String baseUrl, String projectId) {
      this.baseUrl = baseUrl;
      this.projectId = projectId;
    }

    protected Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder dimensions(int dimensions) {
      this.dimensions = dimensions;
      return this;
    }

    public Builder titleProperty(String titleProperty) {
      this.titleProperty = titleProperty;
      return this;
    }

    public Builder taskType(TaskType taskType) {
      this.taskType = taskType;
      return this;
    }

    public Builder sourceProperties(String... properties) {
      return sourceProperties(Arrays.asList(properties));
    }

    /** Add properties to include in the embedding. */
    public Builder sourceProperties(List<String> properties) {
      this.sourceProperties.addAll(properties);
      return this;
    }

    /**
     * Override default vector index configuration.
     *
     * <a href=
     * "https://docs.weaviate.io/weaviate/config-refs/indexing/vector-index#hnsw-index-parameters">HNSW</a>
     * is the default vector index.
     */
    public Builder vectorIndex(VectorIndex vectorIndex) {
      this.vectorIndex = vectorIndex;
      return this;
    }

    public Builder quantization(Quantization quantization) {
      this.quantization = quantization;
      return this;
    }

    public Text2VecGoogleVectorizer build() {
      return new Text2VecGoogleVectorizer(this);
    }
  }

  public static class AiStudioBuilder extends Builder {
    public static final String BASE_URL = "generativelanguage.googleapis.com";

    public AiStudioBuilder() {
      super(BASE_URL, null);
    }
  }

  public static class VertexBuilder extends Builder {
    public static final String DEFAULT_BASE_URL = "us-central1-aiplatform.googleapis.com";

    public VertexBuilder(String projectId) {
      super(DEFAULT_BASE_URL, projectId);
    }

    public VertexBuilder baseUrl(String baseUrl) {
      super.baseUrl(baseUrl);
      return this;
    }
  }
}
