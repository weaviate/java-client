package io.weaviate.client6.v1.collections;

import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ContextionaryVectorizer extends Vectorizer {
  @SerializedName("text2vec-contextionary")
  private Map<String, Object> configuration;

  public static ContextionaryVectorizer of() {
    return new Builder().build();
  }

  public static ContextionaryVectorizer of(Consumer<Builder> fn) {
    var builder = new Builder();
    fn.accept(builder);
    return builder.build();
  }

  public static class Builder {
    private boolean vectorizeCollectionName = false;

    public Builder vectorizeCollectionName() {
      this.vectorizeCollectionName = true;
      return this;
    }

    public ContextionaryVectorizer build() {
      return new ContextionaryVectorizer(Map.of("vectorizeClassName", vectorizeCollectionName));
    }
  }
}
