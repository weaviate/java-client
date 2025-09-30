package io.weaviate.client6.v1.api.backup;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

final class Waiter {

  private final Backup backup;
  private final Callable<Optional<Backup>> poll;
  private final WaitOptions wait;

  Waiter(final Backup backup, Callable<Optional<Backup>> poll, WaitOptions wait) {
    this.backup = backup;
    this.poll = poll;
    this.wait = wait;
  }

  Backup waitForStatus(BackupStatus wantStatus) throws IOException, TimeoutException {
    if (backup.error() != null) {
      throw new RuntimeException(backup.error());
    }

    if (backup.status() == wantStatus) {
      return backup;
    }

    Instant deadline = Instant.now().plusMillis(wait.timeout());
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
        System.out.println("Interrupted");
        Thread.currentThread().interrupt();
      }
    }
    return latest;
  }

  private boolean isComplete(final Backup backup) {
    return backup.status() == BackupStatus.SUCCESS
        || backup.status() == BackupStatus.FAILED
        || backup.status() == BackupStatus.CANCELED;
  }
}
