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

  /**
   * Create new {@link ReuseTokenProvider} from another {@link TokenProvider}.
   * Wrapping an instance ReuseTokenProvider returns that instance,
   * so this method is safe to call with any TokenProvider.
   */
  static TokenProvider wrap(Token t, TokenProvider tp) {
    if (tp instanceof ReuseTokenProvider rtp) {
      if (t == null) {
        return rtp; // Use it directly.
      }
    }

    if (t == null) {
      t = tp.getToken();
    }
    return new ReuseTokenProvider(t, tp);
  }

  private ReuseTokenProvider(Token t, TokenProvider tp) {
    this.provider = tp;
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
