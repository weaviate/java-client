package io.weaviate.client6.v1.api.tokenize;

import java.io.IOException;
import java.util.function.Function;

import io.weaviate.client6.v1.api.WeaviateApiException;
import io.weaviate.client6.v1.internal.ObjectBuilder;
import io.weaviate.client6.v1.internal.rest.RestTransport;

public class WeaviateTokenizeClient {
  private final RestTransport restTransport;

  public WeaviateTokenizeClient(RestTransport restTransport) {
    this.restTransport = restTransport;
  }

  /**
   * Tokenize a text string.
   *
   * @param text       Input text string.
   * @param collection Name of the reference collection.
   * @param property   Name of the property to source tokenization config from.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public TokenizeResponse text(String text, String collection, String property) throws IOException {
    return text(new TokenizeRequest(text, collection, property));
  }

  /**
   * Tokenize a text string.
   *
   * @param text Input text string.
   * @param fn   Lambda expression for optional stopwords.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public TokenizeResponse text(String text, Function<TokenizeRequest.Builder, ObjectBuilder<TokenizeRequest>> fn)
      throws IOException {
    return text(TokenizeRequest.of(text, fn));
  }

  /**
   * Tokenize a text string.
   *
   * @param request Request body.
   * @throws WeaviateApiException in case the server returned with an
   *                              error status code.
   * @throws IOException          in case the request was not sent successfully
   *                              due to a malformed request, a networking error
   *                              or the server being unavailable.
   */
  public TokenizeResponse text(TokenizeRequest request) throws IOException {
    return this.restTransport.performRequest(request, TokenizeRequest._ENDPOINT);
  }
}
