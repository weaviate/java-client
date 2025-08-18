package io.weaviate.client6.v1.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.weaviate.client6.v1.internal.TokenProvider.Token;

/**
 * AsyncTokenProvider obtains authentication tokens asynchronously
 * and can be used in a non-blocking context.
 *
 * As implementors are likely to schedule token fetches on a thread pool,
 * instances must be closed to avoid resource leaks.
 */
public interface AsyncTokenProvider extends AutoCloseable {
  CompletableFuture<Token> getToken();

  CompletableFuture<Token> getToken(Executor executor);

  /**
   * Create an {@link AsyncTokenProvider} instance from an existing
   * {@link TokenProvider}. The inner provider MAY be called from
   * multiple instances and MUST be thread-safe.
   *
   * Either use in a try-with-resources block or close after usage explicitly.
   */
  static AsyncTokenProvider wrap(TokenProvider tp) {
    return new Default(tp);
  }

  /**
   * AsyncTokenProvider fetches tokens in a
   * <i>shared single background thread</i>.
   */
  public static class Default implements AsyncTokenProvider {
    /**
     * Shared executor service. This way all instances
     * can share the same thread pool.
     */
    private static ExecutorService exec;
    /** Shut down {@link #exec} once refCount reaches 0. */
    private static int refCount = 0;

    private final TokenProvider provider;

    Default(TokenProvider tp) {
      synchronized (Default.class) {
        if (refCount == 0) {
          exec = Executors.newSingleThreadExecutor();
        }
        refCount++;
      }
      this.provider = tp;
    }

    /** Get token with the default single-thread executor. */
    @Override
    public CompletableFuture<Token> getToken() {
      return getToken(exec);
    }

    /** Get token with a custom executor. */
    @Override
    public CompletableFuture<Token> getToken(Executor executor) {
      return CompletableFuture.supplyAsync(provider::getToken, executor);
    }

    @Override
    public void close() throws Exception {
      provider.close();

      synchronized (Default.class) {
        refCount--;
        if (refCount == 0 && exec != null) {
          exec.shutdown();
          exec = null;
        }
      }
    }
  }
}
