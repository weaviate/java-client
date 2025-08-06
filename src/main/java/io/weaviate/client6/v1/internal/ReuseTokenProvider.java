package io.weaviate.client6.v1.internal;

/**
 * ReuseTokenProvider returns the same token as long as its valid and obtains a
 * new token from a {@link TokenProvider} otherwise.
 */
final class ReuseTokenProvider implements TokenProvider {
  private TokenProvider provider;
  private Token token;

  // TODO: this will need synchronization
  @Override
  public Token getToken() {
    if (token.isValid()) {
      return token;
    }
    token = provider.getToken();
    return token;
  }
}
