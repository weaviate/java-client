package io.weaviate.integration;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.backup.Backup;
import io.weaviate.client6.v1.api.backup.BackupStatus;
import io.weaviate.client6.v1.api.backup.CompressionLevel;
import io.weaviate.containers.Weaviate;

public class BackupITest extends ConcurrentTest {
  private static final WeaviateClient client = Weaviate.custom()
      .withFilesystemBackup("/tmp/backups").build()
      .getClient();

  @Test
  public void test_lifecycle() throws IOException, TimeoutException {
    // Arrange
    String nsA = ns("A"), nsB = ns("B"), nsC = ns("C"), nsBig = ns("Big");
    String backup_1 = ns("backup_1").toLowerCase();
    String backend = "filesystem";

    // Start writing data in the background so it's ready
    // by the time we get to backup #3.
    var spam = spamData(nsBig);

    client.collections.create(nsA);
    client.collections.create(nsB);
    client.collections.create(nsC);

    // Insert some data to check restore later
    var collectionA = client.collections.use(nsA);
    collectionA.data.insert(Map.of());

    // Act: start backup
    var started = client.backup.create(backup_1, backend,
        backup -> backup
            .excludeCollections(nsC, nsBig)
            .compressionLevel(CompressionLevel.BEST_SPEED));

    // Assert
    Assertions.assertThat(started.backup())
        .as("created backup operation")
        .returns(backup_1, Backup::id)
        .returns(backend, Backup::backend)
        .returns(BackupStatus.STARTED, Backup::status)
        .returns(null, Backup::error)
        .extracting(Backup::includesCollections, InstanceOfAssertFactories.list(String.class))
        .containsOnly(nsA, nsB);

    // Act: await backup competion
    var completed = started.waitForCompletion();

    // Assert
    Assertions.assertThat(completed)
        .as("await backup completion")
        .returns(backup_1, Backup::id)
        .returns(backend, Backup::backend)
        .returns(BackupStatus.SUCCESS, Backup::status)
        .returns(null, Backup::error);

    // Act: create another backup
    String backup_2 = ns("backup_2").toLowerCase();
    client.backup.create(backup_2, backend).waitForCompletion();

    // Assert: check the second backup is created successfully
    var status_2 = client.backup.getCreateStatus(backup_2, backend);
    Assertions.assertThat(status_2).as("backup #2").get()
        .returns(BackupStatus.SUCCESS, Backup::status);

    // Act: create and cancel
    // Try to throttle this backup by creating a lot of objects,
    // limiting CPU resources and requiring high compression ratio.
    // This is to avoid flaky tests and make sure we can cancel
    // the backup before it completes successfully.
    String backup_3 = ns("backup_3").toLowerCase();
    spam.join();
    var cancelMe = client.backup.create(backup_3, backend,
        backup -> backup
            .includeCollections(nsA, nsB, nsC, nsBig)
            .cpuPercentage(1)
            .compressionLevel(CompressionLevel.BEST_COMPRESSION));
    cancelMe.cancel();
    cancelMe.waitForStatus(BackupStatus.CANCELED);

    // Assert: check the backup is cancelled
    var status_3 = client.backup.getCreateStatus(backup_3, backend);
    Assertions.assertThat(status_3).as("backup #3").get()
        .returns(BackupStatus.CANCELED, Backup::status);

    // Assert: all 3 backups are present
    var all = client.backup.list(backend);
    Assertions.assertThat(all).as("all backups")
        .hasSize(3)
        .extracting(Backup::id)
        .containsOnly(backup_1, backup_2, backup_3);

    // Act: delete data and restore backup #1
    client.collections.delete(nsA);
    client.backup.restore(backup_1, backend, restore -> restore.includeCollections(nsA))
        .waitForCompletion();

    // Assert: object inserted in the beginning of the test is present
    var restore_1 = client.backup.getRestoreStatus(backup_1, backend);
    Assertions.assertThat(restore_1).as("restore backup #1").get()
        .returns(BackupStatus.SUCCESS, Backup::status);
    Assertions.assertThat(collectionA.size()).as("after restore backup #1").isEqualTo(1);
  }

  private CompletableFuture<Void> spamData(String collectionName) {
    return CompletableFuture.supplyAsync(() -> {
      var spam = client.collections.use(collectionName);
      for (int i = 0; i < 10_000; i++) {
        var uuids = IntStream.range(0, 10).mapToObj(j -> UUID.randomUUID()).toArray();
        try {
          spam.data.insert(Map.of("uuids", uuids));
        } catch (IOException e) {
          throw new CompletionException(e);
        }
      }
      return null;
    });
  }
}
