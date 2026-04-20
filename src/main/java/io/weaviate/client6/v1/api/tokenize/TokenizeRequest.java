package io.weaviate.client6.v1.api.tokenize;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.collections.InvertedIndex.Stopwords;
import io.weaviate.client6.v1.api.collections.TextAnalyzer;
import io.weaviate.client6.v1.api.collections.Tokenization;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.json.JSON;
import io.weaviate.client6.v1.internal.rest.Endpoint;
import io.weaviate.client6.v1.internal.rest.SimpleEndpoint;

public class TokenizeRequest {
  @SerializedName("text")
  private final String text;

  // These two fields are passed as path parameters.
  private final transient String collection;
  private final transient String property;

  @SerializedName("tokenization")
  private final Tokenization tokenization;
  @SerializedName("analyzerConfig")
  private final TextAnalyzer textAnalyzer;
  @SerializedName("stopwordPresets")
  private final Map<String, Stopwords> stopwordConfig;

  public TokenizeRequest(String text, String collection, String property) {
    this.text = text;
    this.collection = collection;
    this.property = property;
    this.tokenization = null;
    this.textAnalyzer = null;
    this.stopwordConfig = null;
  }

  public TokenizeRequest(
      String text,
      Tokenization tokenization,
      TextAnalyzer textAnalyzer,
      Map<String, Stopwords> stopwordConfig) {
    this.text = text;
    this.collection = null;
    this.property = null;
    this.tokenization = tokenization;
    this.textAnalyzer = textAnalyzer;
    this.stopwordConfig = stopwordConfig;
  }

  public final static Endpoint<TokenizeRequest, TokenizeResponse> _ENDPOINT = new SimpleEndpoint<>(
      __ -> "POST",
      request -> request.collection != null
          ? "/schema/" + request.collection + "/properties/" + request.property + "/tokenize"
          : "/tokenize",
      __ -> Collections.emptyMap(),
      request -> JSON.serialize(
          request.collection != null
              ? Map.of("text", request.text)
              : request),
      (statusCode, response) -> JSON.deserialize(response, TokenizeResponse.class));

  public static final TokenizeRequest of(String text, Function<Builder, ObjectBuilder<TokenizeRequest>> fn) {
    return fn.apply(new Builder(text)).build();
  }

  public TokenizeRequest(Builder builder) {
    this(builder.text, builder.tokenization, builder.textAnalyzer, builder.stopwordConfig);
  }

  public static class Builder implements ObjectBuilder<TokenizeRequest> {
    private final String text;
    private Tokenization tokenization;
    private TextAnalyzer textAnalyzer;
    private Map<String, Stopwords> stopwordConfig = new HashMap<>();

    public Builder tokenization(Tokenization tokenization) {
      this.tokenization = tokenization;
      return this;
    }

    public Builder textAnalyzer(TextAnalyzer textAnalyzer) {
      this.textAnalyzer = textAnalyzer;
      return this;
    }

    public Builder stopwordConfig(Map<String, Stopwords> stopwordConfig) {
      this.stopwordConfig = stopwordConfig;
      return this;
    }

    public Builder(String text) {
      this.text = text;
    }

    @Override
    public TokenizeRequest build() {
      return new TokenizeRequest(this);
    }
  }
}
