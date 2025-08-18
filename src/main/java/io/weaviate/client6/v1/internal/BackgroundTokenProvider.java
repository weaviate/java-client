package io.weaviate.client6.v1.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackgroundTokenProvider implements TokenProvider {
  private final ScheduledExecutorService exec;
  private final TokenProvider provider;

  /**
   * Create a background task to periodically refresh the token.
   *
   * <p>
   * This method will wrap the TokenProvider in {@link ReuseTokenProvider}
   * before passing it on to the constructor to cache the token
   * in-between the refreshes.
   *
   * If TokenProvider is an instance of BackgroundTokenProvider
   * it is returned immediately.
   */
  public static TokenProvider wrap(TokenProvider tp) {
    if (tp instanceof BackgroundTokenProvider) {
      return tp;
    }
    return new BackgroundTokenProvider(ReuseTokenProvider.wrap(null, tp));
  }

  private BackgroundTokenProvider(TokenProvider tp) {
    this.provider = tp;
    this.exec = Executors.newSingleThreadScheduledExecutor();

    scheduleNextRefresh();
  }

  @Override
  public Token getToken() {
    return provider.getToken();
  }

  /**
   * Fetch the token and schedule a task to refresh it
   * after {@link Token#expiresIn} seconds. The next
   * refresh task is scheduled immediately afterwards.
   *
   * If {@link Token#neverExpires} this method returns
   * early and the next refresh task is never scheduled.
   */
  private void scheduleNextRefresh() {
    var t = getToken();
    if (t.neverExpires()) {
      return;
    }
    exec.schedule(this::scheduleNextRefresh, t.expiresIn(), TimeUnit.SECONDS);
  }

  @Override
  public void close() throws Exception {
    System.out.println("BackgroundTokenProvider::close");
    exec.shutdown();
  }
}
