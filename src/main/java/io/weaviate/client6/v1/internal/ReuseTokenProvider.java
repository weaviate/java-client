package io.weaviate.client6.v1.internal;

import javax.annotation.concurrent.ThreadSafe;

/**
 * ReuseTokenProvider returns the same token as long as its valid and obtains a
 * new token from a {@link TokenProvider} otherwise.
 */
@ThreadSafe
final class ReuseTokenProvider implements TokenProvider {
  private final TokenProvider provider;

  private volatile Token token;

  public static TokenProvider wrap(Token t, TokenProvider provider) {
    if (provider instanceof ReuseTokenProvider rtp) {
      if (t == null) {
        return rtp; // Use it directly.
      }
    }

    if (t == null) {
      t = provider.getToken();
    }
    return new ReuseTokenProvider(t, provider);
  }

  private ReuseTokenProvider(Token t, TokenProvider provider) {
    this.provider = provider;
    this.token = token;
  }

  @Override
  public Token getToken() {
    if (token.isValid()) {
      return token;
    }
    synchronized (this) {
      if (!token.isValid()) {
        token = provider.getToken();
      }
    }
    return token;
  }
}
