package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

abstract class RetriableTask {
  private final String id;
  private final CompletableFuture<Void> root;

  private volatile CompletableFuture<Void> current = new CompletableFuture<>();
  private volatile int retries = 0;

  protected RetriableTask(String id, RetryPolicy retryPolicy, Consumer<String> onRetry) {
    this.id = requireNonNull(id, "id is null");
    this.root = retry(current, retryPolicy, onRetry);
  }

  private final CompletableFuture<Void> retry(
      CompletableFuture<Void> f,
      RetryPolicy retryPolicy,
      Consumer<String> onRetry) {
    requireNonNull(f, "f is null");
    requireNonNull(retryPolicy, "retryPolicy is null");
    requireNonNull(onRetry, "onRetry is null");

    return f.exceptionallyCompose(t -> {
      if (!retryPolicy.canRetry(this, t)) {
        return CompletableFuture.failedFuture(t);
      }
      retries++;
      current = new CompletableFuture<>();
      onRetry.accept(id);
      return retry(current, retryPolicy, onRetry);
    });
  }

  /** Number of times this task has been retried. */
  public final int timesRetried() {
    return retries;
  }

  /** Retrieve the ID of this task. */
  public final String id() {
    return id;
  }

  /**
   * Mark the task successful. This status cannot be changed, so calling
   * {@link #setError} afterwards will have no effect.
   */
  public final boolean setSuccess() {
    return current.complete(null);
  }

  /**
   * Mark the task failed. This status cannot be changed, so calling
   * {@link #setSuccess} afterwards will have no effect.
   *
   * @param error Error message. Null values are tolerated, but are only expected
   *              to occur due to a server's mistake.
   *              Do not use {@code setError(null)} if the server reports success
   *              status for the task; prefer {@link #setSuccess} in that case.
   */
  public final boolean setError(Throwable t) {
    return current.completeExceptionally(t);
  }

  /**
   * Track completion of this task.
   *
   * @return A future which completes when the server reports success
   *         for this tasks or the applied {@link RetryPolicy}
   *         no longer permits retrying the task.
   */
  public final CompletableFuture<Void> done() {
    return root;
  }
}
