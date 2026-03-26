package io.weaviate.client6.v1.api.collections.batch;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

public class RetryPolicy {
  /** Create a retry policy that never permits retrying a task. */
  static RetryPolicy never() {
    return new RetryPolicy(__ -> false);
  }

  private final Predicate<RetriableTask> retry;

  /**
   * Construct a simple RetryPolicy that retries up to a certain number of times.
   *
   * @param maxRetries Maximum number of retries.
   */
  public RetryPolicy(int maxRetries) {
    this(task -> task.timesRetried() < maxRetries);
  }

  /**
   * Construct a RetryPolicy with a custom predicate.
   *
   * @param retry Predicate that returns true if the task should be retried.
   */
  public RetryPolicy(Predicate<RetriableTask> retry) {
    this.retry = requireNonNull(retry, "retry is null");
  }

  /**
   * Override this method to control which exceptions are considered retriable.
   */
  protected boolean canRetryThrowable(Throwable t) {
    return t instanceof ServerException;
  }

  boolean canRetry(RetriableTask task, Throwable t) {
    return canRetryThrowable(t) && retry.test(task);
  }
}
