package io.weaviate.client6.v1.api.collections.rerankers;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Reranker;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record NvidiaReranker(
    @SerializedName("model") String model,
    @SerializedName("baseUrl") String baseUrl) implements Reranker {

  @Override
  public Kind _kind() {
    return Reranker.Kind.NVIDIA;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static NvidiaReranker of() {
    return of(ObjectBuilder.identity());
  }

  public static NvidiaReranker of(Function<Builder, ObjectBuilder<NvidiaReranker>> fn) {
    return fn.apply(new Builder()).build();
  }

  public NvidiaReranker(Builder builder) {
    this(builder.model, builder.baseUrl);
  }

  public static class Builder implements ObjectBuilder<NvidiaReranker> {
    private String model;
    private String baseUrl;

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    @Override
    public NvidiaReranker build() {
      return new NvidiaReranker(this);
    }
  }
}
