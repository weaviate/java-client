package io.weaviate.client6.v1.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Img2VecNeuralVectorizer extends Vectorizer {
  @SerializedName("img2vec-neural")
  private Map<String, Object> configuration;

  public static Img2VecNeuralVectorizer of() {
    return new Builder().build();
  }

  public static Img2VecNeuralVectorizer of(Consumer<Builder> fn) {
    var builder = new Builder();
    fn.accept(builder);
    return builder.build();
  }

  public static class Builder {
    private List<String> imageFields = new ArrayList<>();

    public Builder imageFields(String... fields) {
      this.imageFields = Arrays.asList(fields);
      return this;
    }

    public Img2VecNeuralVectorizer build() {
      return new Img2VecNeuralVectorizer(Map.of("imageFields", imageFields));
    }
  }
}
