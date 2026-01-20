package io.weaviate.client6.v1.api.backup;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

final class Waiter {

  private final Backup backup;
  private final WaitOptions wait;

  Waiter(final Backup backup, WaitOptions wait) {
    this.backup = backup;
    this.wait = wait;
  }

  Backup waitForStatus(final BackupStatus wantStatus, Callable<Optional<Backup>> poll)
      throws IOException, TimeoutException {
    if (backup.error() != null) {
      throw new RuntimeException(backup.error());
    }

    if (backup.status() == wantStatus) {
      return backup;
    }

    final Instant deadline = Instant.now().plusMillis(wait.timeout());
    Backup latest = backup;
    while (!Thread.interrupted()) {
      if (Instant.now().isAfter(deadline)) {
        throw new TimeoutException("timed out after %s, latest status %s".formatted(
            Duration.ofMillis(wait.timeout()).toSeconds(), latest.status()));
      }

      try {
        var current = poll.call().orElseThrow();
        latest = current;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      if (latest.status() == wantStatus) {
        return latest;
      } else if (isComplete(latest)) {
        throw new IllegalStateException("completed with status=%s without reaching %s"
            .formatted(latest.status(), wantStatus));
      }

      try {
        Thread.sleep(wait.interval());
      } catch (InterruptedException e) {
        // TODO: the interrupted state will be cleared on the next while() check
        // and then we will simply return the latest state. An absence of an exception
        // might be misleading here. What should we do?
        Thread.currentThread().interrupt();
      }
    }
    return latest;
  }

  CompletableFuture<Backup> waitForStatusAsync(
      final BackupStatus wantStatus,
      Supplier<CompletableFuture<Optional<Backup>>> poll) {
    if (backup.status() == wantStatus) {
      return CompletableFuture.completedFuture(backup);
    }
    final Instant deadline = Instant.now().plusMillis(wait.timeout());
    return poll.get().thenCompose(latest -> _waitForStatusAsync(wantStatus, latest.orElseThrow(), poll, deadline));
  }

  CompletableFuture<Backup> _waitForStatusAsync(
      final BackupStatus wantStatus,
      final Backup current,
      Supplier<CompletableFuture<Optional<Backup>>> poll,
      final Instant deadline) {

    if (current.status() == wantStatus) {
      return CompletableFuture.completedFuture(current);
    }

    if (Instant.now().isAfter(deadline)) {
      var e = new TimeoutException("timed out after %s, latest status %s".formatted(
          Duration.ofMillis(wait.timeout()).toSeconds(), current.status()));
      throw new CompletionException(e);
    }

    return poll.get().thenComposeAsync(
        latest -> _waitForStatusAsync(wantStatus, latest.orElseThrow(), poll, deadline),
        CompletableFuture.delayedExecutor(wait.interval(), TimeUnit.MILLISECONDS));
  }

  private boolean isComplete(final Backup backup) {
    return backup.status() == BackupStatus.SUCCESS
        || backup.status() == BackupStatus.FAILED
        || backup.status() == BackupStatus.CANCELED;
  }
}
