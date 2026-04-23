package io.weaviate.client6.v1.api.tokenize;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateTokenizeClientAsync {
  private final RestTransport restTransport;

  public WeaviateTokenizeClientAsync(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Tokenize a text string.
   *
   * @param text       Input text string.
   * @param collection Name of the reference collection.
   * @param property   Name of the property to source tokenization config from.
   */
  public CompletableFuture<TokenizeResponse> forProperty(String text, String collection, String property) {
    return text(new TokenizeRequest(text, collection, property));
  }

  /**
   * Tokenize a text string.
   *
   * @param text Input text string.
   * @param fn   Lambda expression for optional tokenization parameters.
   */
  public CompletableFuture<TokenizeResponse> text(String text,
      Function<TokenizeRequest.Builder, ObjectBuilder<TokenizeRequest>> fn) {
    return text(TokenizeRequest.of(text, fn));
  }

  /**
   * Tokenize a text string.
   *
   * @param request Request body.
   */
  public CompletableFuture<TokenizeResponse> text(TokenizeRequest request) {
    return this.restTransport.performRequestAsync(request, TokenizeRequest._ENDPOINT);
  }
}
