package io.weaviate.client6.v1.api.tokenize;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
  @SerializedName("stopwords")
  private final Stopwords stopwords;
  @SerializedName("stopwordPresets")
  private final Map<String, List<String>> stopwordPresets;

  public TokenizeRequest(String text, String collection, String property) {
    this.text = text;
    this.collection = collection;
    this.property = property;
    this.tokenization = null;
    this.textAnalyzer = null;
    this.stopwords = null;
    this.stopwordPresets = null;
  }

  public TokenizeRequest(
      String text,
      Tokenization tokenization,
      TextAnalyzer textAnalyzer,
      Stopwords stopwords,
      Map<String, List<String>> stopwordPresets) {
    this.text = text;
    this.collection = null;
    this.property = null;
    this.tokenization = tokenization;
    this.textAnalyzer = textAnalyzer;
    this.stopwords = stopwords;
    this.stopwordPresets = stopwordPresets;
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
    this(
        builder.text,
        builder.tokenization,
        builder.textAnalyzer,
        builder.stopwords,
        builder.stopwordPresets);
  }

  public static class Builder implements ObjectBuilder<TokenizeRequest> {
    private final String text;
    private Tokenization tokenization;
    private TextAnalyzer textAnalyzer;
    private Stopwords stopwords;
    private Map<String, List<String>> stopwordPresets = new HashMap<>();

    /** Set tokenization strategy. */
    public Builder tokenization(Tokenization tokenization) {
      this.tokenization = tokenization;
      return this;
    }

    /** Configure ASCII character folding. */
    public Builder textAnalyzer(TextAnalyzer textAnalyzer) {
      this.textAnalyzer = textAnalyzer;
      return this;
    }

    /**
     * Select a stopwords preset. Mutually exclusive with {@link #stopwordPresets}.
     */
    public Builder stopwords(Stopwords stopwords) {
      this.stopwords = stopwords;
      this.stopwordPresets.clear();
      return this;
    }

    /**
     * Select multiple stopword presets. Mutually exclusive with {@link #stopwords}.
     */
    public Builder stopwordPresets(Map<String, List<String>> stopwordPresets) {
      this.stopwords = null;
      this.stopwordPresets = stopwordPresets;
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
