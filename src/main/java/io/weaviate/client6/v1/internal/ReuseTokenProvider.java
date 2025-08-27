package io.weaviate.client6.v1.internal;

import javax.annotation.concurrent.ThreadSafe;

/**
 * ReuseTokenProvider returns the same token as long as its valid and obtains a
 * new token from a {@link TokenProvider} otherwise.
 *
 * <p>
 * Usage:
 *
 * <pre>{@code
 * // Create an TokenProvider that can rotate tokens as they expire.
 * var myProvider = new MyTokenProvider();
 *
 * // Create a reusable TokenProvider.
 * var tokenProvider = ReuseTokenProvider.wrap(myProvider);
 * }</pre>
 */
@ThreadSafe
final class ReuseTokenProvider implements TokenProvider {
  private final TokenProvider provider;
  private final long expiryDelta;

  private volatile Token token;

  /**
   * Create new {@link ReuseTokenProvider} from another {@link TokenProvider}.
   * Wrapping an instance ReuseTokenProvider returns that instance if the token is
   * {@code null}, so this method is safe to call with any TokenProvider.
   *
   * @return A TokenProvider.
   */
  static TokenProvider wrap(Token t, TokenProvider tp, long expiryDelta) {
    if (tp instanceof ReuseTokenProvider rtp) {
      if (t == null) {
        // Use it directly, but set new expiry delta.
        return rtp.withExpiryDelta(expiryDelta);
      }
    }
    return new ReuseTokenProvider(t, tp, expiryDelta);
  }

  /**
   * Create new {@link ReuseTokenProvider} from another {@link TokenProvider}.
   * Wrapping an instance ReuseTokenProvider returns that instance if the token is
   * {@code null}, so this method is safe to call with any TokenProvider.
   */
  static TokenProvider wrap(Token t, TokenProvider tp) {
    if (tp instanceof ReuseTokenProvider rtp) {
      if (t == null) {
        return rtp; // Use it directly.
      }
    }
    return new ReuseTokenProvider(t, tp, 0);
  }

  /**
   * Create a new TokenProvider with a different expiryDelta.
   * Tokens obtained from this TokenProvider with have the same early expiry.
   *
   * @param expiryDelta Early expiry in seconds.
   * @return A new TokenProvider.
   */
  TokenProvider withExpiryDelta(long expirtyDelta) {
    return new ReuseTokenProvider(this.token, this.provider, expirtyDelta);
  }

  private ReuseTokenProvider(Token t, TokenProvider tp, long expiryDelta) {
    this.provider = tp;
    this.token = t;
    this.expiryDelta = expiryDelta;
  }

  @Override
  public Token getToken() {
    if (token != null && token.isValid()) {
      return token;
    }
    synchronized (this) {
      if (token == null || !token.isValid()) {
        token = provider.getToken().withExpiryDelta(expiryDelta);
      }
    }
    return token;
  }

  @Override
  public void close() throws Exception {
    provider.close();
  }
}
