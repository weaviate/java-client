package io.weaviate.client6.v1.api.backup;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.internal.ObjectBuilder;

public record Backup(
    @SerializedName("id") String id,
    @SerializedName("path") String path,
    @SerializedName("backend") String backend,
    @SerializedName("classes") List<String> includesCollections,
    @SerializedName("status") BackupStatus status,
    @SerializedName("error") String error,
    @SerializedName("__operation__") Operation operation) {

  public Backup withOperation(Operation operation) {
    return new Backup(id, path, backend, includesCollections, status, error, operation);
  }

  public enum Operation {
    CREATE, RESTORE;
  }

  public Backup waitForCompletion(WeaviateClient client) throws IOException, TimeoutException {
    return waitForStatus(client, BackupStatus.SUCCESS);
  }

  public Backup waitForCompletion(WeaviateClient client, Function<WaitOptions.Builder, ObjectBuilder<WaitOptions>> fn)
      throws IOException, TimeoutException {
    return waitForStatus(client, BackupStatus.SUCCESS, fn);
  }

  public Backup waitForStatus(WeaviateClient client, BackupStatus status) throws IOException, TimeoutException {
    return waitForStatus(client, status, ObjectBuilder.identity());
  }

  public Backup waitForStatus(WeaviateClient client, BackupStatus status,
      Function<WaitOptions.Builder, ObjectBuilder<WaitOptions>> fn) throws IOException, TimeoutException {
    if (operation == null) {
      throw new IllegalStateException("backup.operation is null");
    }

    final var options = WaitOptions.of(fn);
    final Callable<Optional<Backup>> poll = operation == Operation.CREATE
        ? () -> client.backup.getCreateStatus(id, backend)
        : () -> client.backup.getRestoreStatus(id, backend);
    return new Waiter(this, poll, options).waitForStatus(status);
  }

  public void cancel(WeaviateClient client) throws IOException {
    if (operation == Operation.RESTORE) {
      throw new IllegalStateException("backup restore cannot be canceled");
    }
    client.backup.cancel(id(), backend());
  }
}
