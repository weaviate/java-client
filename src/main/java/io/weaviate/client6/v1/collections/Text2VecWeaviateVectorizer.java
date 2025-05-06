package io.weaviate.client6.v1.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Text2VecWeaviateVectorizer extends Vectorizer {
  @SerializedName("text2vec-weaviate")
  private Map<String, Object> configuration;

  public static Text2VecWeaviateVectorizer of() {
    return new Builder().build();
  }

  public static Text2VecWeaviateVectorizer of(Consumer<Builder> fn) {
    var builder = new Builder();
    fn.accept(builder);
    return builder.build();
  }

  public static final String SNOWFLAKE_ARCTIC_EMBED_L_20 = "Snowflake/snowflake-arctic-embed-l-v2.0";
  public static final String SNOWFLAKE_ARCTIC_EMBED_M_15 = "Snowflake/snowflake-arctic-embed-m-v1.5";

  public static class Builder {
    private boolean vectorizeCollectionName = false;
    private String baseUrl;
    private Integer dimensions;
    private String model;

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
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

    public Builder vectorizeCollectionName() {
      this.vectorizeCollectionName = true;
      return this;
    }

    public Text2VecWeaviateVectorizer build() {
      return new Text2VecWeaviateVectorizer(new HashMap<>() {
        {
          put("vectorizeClassName", vectorizeCollectionName);
          if (baseUrl != null) {
            put("baseURL", baseUrl);
          }
          if (dimensions != null) {
            put("dimensions", dimensions);
          }
          if (model != null) {
            put("model", model);
          }
        }
      });
    }
  }
}
