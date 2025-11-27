package io.weaviate.client6.v1.api.collections.rerankers;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Reranker;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record CohereReranker(
    @SerializedName("model") String model) implements Reranker {

  public static final String RERANK_ENGLISH_V2 = "rerank-english-v2.0";
  public static final String RERANK_MULTILINGUAL_V2 = "rerank-multilingual-v2.0";

  @Override
  public Kind _kind() {
    return Reranker.Kind.COHERE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static CohereReranker of() {
    return of(ObjectBuilder.identity());
  }

  public static CohereReranker of(Function<Builder, ObjectBuilder<CohereReranker>> fn) {
    return fn.apply(new Builder()).build();
  }

  public CohereReranker(Builder builder) {
    this(builder.model);
  }

  public static class Builder implements ObjectBuilder<CohereReranker> {
    private String model;

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    @Override
    public CohereReranker build() {
      return new CohereReranker(this);
    }
  }
}
