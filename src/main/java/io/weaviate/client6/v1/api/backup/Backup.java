package io.weaviate.client6.v1.api.backup;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client6.v1.api.WeaviateClient;

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

  public Backup waitForStatus(WeaviateClient client, BackupStatus status) throws IOException, TimeoutException {
    final Callable<Optional<Backup>> poll = operation == Operation.CREATE
        ? () -> client.backup.getCreateStatus(id, backend)
        : () -> client.backup.getRestoreStatus(id, backend);
    return new Waiter(this, poll).waitForStatus(status);
  }

  public void cancel(WeaviateClient client) throws IOException {
    client.backup.cancel(id(), backend());
  }
}
