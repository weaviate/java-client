package io.weaviate.client6.v1.api.collections.vectorizers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Vectorizer;
import io.weaviate.client6.v1.api.collections.vectorindex.WrappedVectorIndex;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Img2VecNeuralVectorizer(
    @SerializedName("imageFields") List<String> imageFields) implements Vectorizer {

  @Override
  public Vectorizer.Kind _kind() {
    return Vectorizer.Kind.IMG2VEC_NEURAL;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static Img2VecNeuralVectorizer of() {
    return of(ObjectBuilder.identity());
  }

  public static Img2VecNeuralVectorizer of(Function<Builder, ObjectBuilder<Img2VecNeuralVectorizer>> fn) {
    return fn.apply(new Builder()).build();
  }

  public Img2VecNeuralVectorizer(Builder builder) {
    this(builder.imageFields);
  }

  public static class Builder extends WrappedVectorIndex.Builder<Builder, Img2VecNeuralVectorizer> {
    private List<String> imageFields = new ArrayList<>();

    public Builder imageFields(List<String> fields) {
      this.imageFields = fields;
      return this;
    }

    public Builder imageFields(String... fields) {
      return imageFields(Arrays.asList(fields));
    }

    @Override
    public Img2VecNeuralVectorizer build() {
      return new Img2VecNeuralVectorizer(this);
    }
  }
}
