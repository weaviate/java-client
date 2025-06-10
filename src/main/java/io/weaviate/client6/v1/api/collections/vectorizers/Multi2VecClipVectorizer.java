package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Multi2VecClipVectorizer(
    @SerializedName("vectorizeClassName") boolean vectorizeCollectionName,
    @SerializedName("inferenceUrl") String inferenceUrl,
    @SerializedName("imageFields") List<String> imageFields,
    @SerializedName("textFields") List<String> textFields,
    @SerializedName("weights") Weights weights) implements Vectorizer {

  private static record Weights(
      @SerializedName("imageWeights") List<Float> imageWeights,
      @SerializedName("textWeights") List<Float> textWeights) {
  }

  @Override
  public Vectorizer.Kind _kind() {
    return Vectorizer.Kind.MULTI2VEC_CLIP;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Multi2VecClipVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Multi2VecClipVectorizer of(Function<Builder, ObjectBuilder<Multi2VecClipVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Multi2VecClipVectorizer(Builder builder) {
    this(
        builder.vectorizeCollectionName,
        builder.inferenceUrl,
        builder.imageFields.keySet().stream().toList(),
        builder.textFields.keySet().stream().toList(),
        new Weights(
            builder.imageFields.values().stream().toList(),
            builder.textFields.values().stream().toList()));
  }

  public static class Builder implements ObjectBuilder<Multi2VecClipVectorizer> {
    private boolean vectorizeCollectionName = false;
    private String inferenceUrl;
    private Map<String, Float> imageFields = new HashMap<>();
    private Map<String, Float> textFields = new HashMap<>();

    public Builder inferenceUrl(String inferenceUrl) {
      this.inferenceUrl = inferenceUrl;
      return this;
    }

    public Builder imageFields(List<String> fields) {
      fields.forEach(field -> imageFields.put(field, null));
      return this;
    }

    public Builder imageFields(String... fields) {
      return imageFields(Arrays.asList(fields));
    }

    public Builder imageField(String field, float weight) {
      imageFields.put(field, weight);
      return this;
    }

    public Builder textFields(List<String> fields) {
      fields.forEach(field -> textFields.put(field, null));
      return this;
    }

    public Builder textFields(String... fields) {
      return textFields(Arrays.asList(fields));
    }

    public Builder textField(String field, float weight) {
      textFields.put(field, weight);
      return this;
    }

    public Builder vectorizeCollectionName(boolean enable) {
      this.vectorizeCollectionName = enable;
      return this;
    }

    @Override
    public Multi2VecClipVectorizer build() {
      return new Multi2VecClipVectorizer(this);
    }
  }
}
