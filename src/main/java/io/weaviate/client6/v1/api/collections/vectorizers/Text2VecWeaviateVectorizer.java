package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.VectorIndex;
import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Text2VecWeaviateVectorizer(
    @SerializedName("baseUrl") String inferenceUrl,
    @SerializedName("dimensions") Integer dimensions,
    @SerializedName("model") String model,
    VectorIndex vectorIndex) implements Vectorizer {

  @Override
  public Vectorizer.Kind _kind() {
    return Vectorizer.Kind.TEXT2VEC_WEAVIATE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Text2VecWeaviateVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Text2VecWeaviateVectorizer of(Function<Builder, ObjectBuilder<Text2VecWeaviateVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Text2VecWeaviateVectorizer(Builder builder) {
    this(
        builder.inferenceUrl,
        builder.dimensions,
        builder.model,
        builder.vectorIndex);
  }

  public static final String SNOWFLAKE_ARCTIC_EMBED_L_20 = "Snowflake/snowflake-arctic-embed-l-v2.0";
  public static final String SNOWFLAKE_ARCTIC_EMBED_M_15 = "Snowflake/snowflake-arctic-embed-m-v1.5";

  public static class Builder implements ObjectBuilder<Text2VecWeaviateVectorizer> {
    private VectorIndex vectorIndex = VectorIndex.DEFAULT_VECTOR_INDEX;
    private String inferenceUrl;
    private Integer dimensions;
    private String model;

    public Builder inferenceUrl(String inferenceUrl) {
      this.inferenceUrl = inferenceUrl;
      return this;
    }

    public Builder dimensions(int dimensions) {
      this.dimensions = dimensions;
      return this;
    }

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder vectorIndex(VectorIndex vectorIndex) {
      this.vectorIndex = vectorIndex;
      return this;
    }

    public Text2VecWeaviateVectorizer build() {
      return new Text2VecWeaviateVectorizer(this);
    }
  }
}
