package io.weaviate.client6.v1.api.collections.generative;

import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record AwsGenerative(
    @SerializedName("region") String region,
    @SerializedName("service") String service,
    @SerializedName("endpoint") String baseURL,
    @SerializedName("model") String model) implements Generative {

  @Override
  public Kind _kind() {
    return Generative.Kind.AWS;
  }

  @Override
  public Object _self() {
    return this;
  }

  public static AwsGenerative of(String region, String service) {
    return of(region, service, ObjectBuilder.identity());
  }

  public static AwsGenerative of(String region, String service, Function<Builder, ObjectBuilder<AwsGenerative>> fn) {
    return fn.apply(new Builder(region, service)).build();
  }

  public AwsGenerative(Builder builder) {
    this(
        builder.service,
        builder.region,
        builder.baseUrl,
        builder.model);
  }

  public static class Builder implements ObjectBuilder<AwsGenerative> {
    private final String region;
    private final String service;

    public Builder(String service, String region) {
      this.service = service;
      this.region = region;
    }

    private String baseUrl;
    private String model;

    /** Base URL of the generative provider. */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /** Select generative model. */
    public Builder model(String model) {
      this.model = model;
      return this;
    }

    @Override
    public AwsGenerative build() {
      return new AwsGenerative(this);
    }
  }
}
