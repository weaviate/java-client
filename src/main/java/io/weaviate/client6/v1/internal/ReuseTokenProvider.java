package io.weaviate.client6.v1.internal;

import javax.annotation.concurrent.ThreadSafe;

/**
 * ReuseTokenProvider returns the same token as long as its valid and obtains a
 * new token from a {@link TokenProvider} otherwise.
 */
@ThreadSafe
final class ReuseTokenProvider implements TokenProvider {
  private TokenProvider provider;
  private Token token;

  @Override
  public synchronized Token getToken() {
    if (token.isValid()) {
      return token;
    }
    token = provider.getToken();
    return token;
  }
}
