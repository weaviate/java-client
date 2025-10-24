package io.weaviate.client6.v1.api.collections.rerankers;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Reranker;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record JinaAiReranker(
    @SerializedName("model") String model) implements Reranker {

  public static final String BASE_MULTILINGUAL_V1 = "jina-reranker-v2-base-multilingual";
  public static final String BASE_ENGLISH_V1 = "jina-reranker-v1-base-en";
  public static final String TURBO_ENGLISH_V1 = "jina-reranker-v1-turbo-en";
  public static final String TINY_ENGLISH_V1 = "jina-reranker-v1-tiny-en";
  public static final String COLBERT_ENGLISH_V1 = "jina-colbert-v1-en";

  @Override
  public Kind _kind() {
    return Reranker.Kind.JINAAI;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static JinaAiReranker of() {
    return of(ObjectBuilder.identity());
  }

  public static JinaAiReranker of(Function<Builder, ObjectBuilder<JinaAiReranker>> fn) {
    return fn.apply(new Builder()).build();
  }

  public JinaAiReranker(Builder builder) {
    this(builder.model);
  }

  public static class Builder implements ObjectBuilder<JinaAiReranker> {
    private String model;

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    @Override
    public JinaAiReranker build() {
      return new JinaAiReranker(this);
    }
  }
}
