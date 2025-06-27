package io.weaviate.client6.v1.api.collections.generative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record CohereGenerative(
    @SerializedName("kProperty") String kProperty,
    @SerializedName("model") String model,
    @SerializedName("maxTokensProperty") Integer maxTokensProperty,
    @SerializedName("returnLikelihoodsProperty") String returnLikelihoodsProperty,
    @SerializedName("stopSequencesProperty") List<String> stopSequencesProperty,
    @SerializedName("temperatureProperty") String temperatureProperty) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.COHERE;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static CohereGenerative of() {
    return of(ObjectBuilder.identity());
  }

  public static CohereGenerative of(Function<Builder, ObjectBuilder<CohereGenerative>> fn) {
    return fn.apply(new Builder()).build();
  }

  public CohereGenerative(Builder builder) {
    this(
        builder.kProperty,
        builder.model,
        builder.maxTokensProperty,
        builder.returnLikelihoodsProperty,
        builder.stopSequencesProperty,
        builder.temperatureProperty);
  }

  public static class Builder implements ObjectBuilder<CohereGenerative> {
    private String kProperty;
    private String model;
    private Integer maxTokensProperty;
    private String returnLikelihoodsProperty;
    private List<String> stopSequencesProperty = new ArrayList<>();
    private String temperatureProperty;

    public Builder kProperty(String kProperty) {
      this.kProperty = kProperty;
      return this;
    }

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder maxTokensProperty(int maxTokensProperty) {
      this.maxTokensProperty = maxTokensProperty;
      return this;
    }

    public Builder returnLikelihoodsProperty(String returnLikelihoodsProperty) {
      this.returnLikelihoodsProperty = returnLikelihoodsProperty;
      return this;
    }

    public Builder stopSequencesProperty(String... stopSequencesProperty) {
      return stopSequencesProperty(Arrays.asList(stopSequencesProperty));
    }

    public Builder stopSequencesProperty(List<String> stopSequencesProperty) {
      this.stopSequencesProperty = stopSequencesProperty;
      return this;
    }

    public Builder temperatureProperty(String temperatureProperty) {
      this.temperatureProperty = temperatureProperty;
      return this;
    }

    @Override
    public CohereGenerative build() {
      return new CohereGenerative(this);
    }
  }
}
