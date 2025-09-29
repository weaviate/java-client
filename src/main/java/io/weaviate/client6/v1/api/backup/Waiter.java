package io.weaviate.client6.v1.api.backup;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

final class Waiter {
  private static final long WAIT_INTERVAL_MILLIS = 1_000;
  private static final long TIMEOUT_MILLIS = 3600_000;

  private final Backup backup;
  private final Callable<Optional<Backup>> poll;

  Waiter(final Backup backup, Callable<Optional<Backup>> poll) {
    this.backup = backup;
    this.poll = poll;
  }

  Backup waitForStatus(BackupStatus wantStatus) throws IOException, TimeoutException {
    if (backup.error() != null) {
      throw new RuntimeException(backup.error());
    }

    if (backup.status() == wantStatus) {
      return backup;
    }

    Instant deadline = Instant.now().plusMillis(TIMEOUT_MILLIS);
    Backup latest = backup;
    while (!Thread.interrupted()) {
      if (Instant.now().isAfter(deadline)) {
        throw new TimeoutException("timed out after %s, latest status %s".formatted(
            Duration.ofMillis(TIMEOUT_MILLIS).toSeconds(), latest.status()));
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
        Thread.sleep(WAIT_INTERVAL_MILLIS);
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
