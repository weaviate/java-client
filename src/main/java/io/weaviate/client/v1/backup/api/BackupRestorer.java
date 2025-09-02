package io.weaviate.client.v1.backup.api;

import com.google.gson.annotations.SerializedName;

import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;
import io.weaviate.client.v1.backup.model.BackupRestoreResponse;
import io.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;
import io.weaviate.client.v1.backup.model.RbacRestoreOption;
import io.weaviate.client.v1.backup.model.RestoreStatus;
import lombok.Builder;
import lombok.Getter;

public class BackupRestorer extends BaseClient<BackupRestoreResponse> implements ClientResult<BackupRestoreResponse> {

  private static final long WAIT_INTERVAL = 1000;

  private final BackupRestoreStatusGetter statusGetter;
  private String[] includeClassNames;
  private String[] excludeClassNames;
  private String backend;
  private String backupId;
  private Boolean overwriteAlias;
  private BackupRestoreConfig config;
  private boolean waitForCompletion;

  public BackupRestorer(HttpClient httpClient, Config config, BackupRestoreStatusGetter statusGetter) {
    super(httpClient, config);
    this.statusGetter = statusGetter;
  }

  public BackupRestorer withIncludeClassNames(String... classNames) {
    this.includeClassNames = classNames;
    return this;
  }

  public BackupRestorer withExcludeClassNames(String... classNames) {
    this.excludeClassNames = classNames;
    return this;
  }

  public BackupRestorer withBackend(String backend) {
    this.backend = backend;
    return this;
  }

  public BackupRestorer withOverwriteAlias(Boolean overwriteAlias) {
    this.overwriteAlias = overwriteAlias;
    return this;
  }

  public BackupRestorer withConfig(BackupRestoreConfig config) {
    this.config = config;
    return this;
  }

  @Deprecated
  public BackupRestorer backend(String backend) {
    return withBackend(backend);
  }

  public BackupRestorer withBackupId(String backupId) {
    this.backupId = backupId;
    return this;
  }

  public BackupRestorer withWaitForCompletion(boolean waitForCompletion) {
    this.waitForCompletion = waitForCompletion;
    return this;
  }

  @Override
  public Result<BackupRestoreResponse> run() {
    BackupRestore payload = BackupRestore.builder()
        .include(includeClassNames)
        .exclude(excludeClassNames)
        .overwriteAlias(overwriteAlias)
        .config(config)
        .build();

    if (waitForCompletion) {
      return restoreAndWaitForCompletion(payload);
    }
    return restore(payload);
  }

  private Result<BackupRestoreResponse> restore(BackupRestore payload) {
    Response<BackupRestoreResponse> response = sendPostRequest(path(), payload, BackupRestoreResponse.class);
    return new Result<>(response);
  }

  private Result<BackupRestoreResponse> restoreAndWaitForCompletion(BackupRestore payload) {
    Result<BackupRestoreResponse> result = restore(payload);
    if (result.hasErrors()) {
      return result;
    }

    statusGetter.withBackend(backend).withBackupId(backupId);
    while (true) {
      Response<BackupRestoreStatusResponse> statusResponse = statusGetter.statusRestore();
      if (new Result<>(statusResponse).hasErrors()) {
        return merge(statusResponse, result);
      }

      switch (statusResponse.getBody().getStatus()) {
        case RestoreStatus.SUCCESS:
        case RestoreStatus.FAILED:
          return merge(statusResponse, result);
      }

      try {
        Thread.sleep(WAIT_INTERVAL);
      } catch (InterruptedException e) {
        return merge(statusResponse, result);
      }
    }
  }

  private String path() {
    return String.format("/backups/%s/%s/restore", backend, backupId);
  }

  private Result<BackupRestoreResponse> merge(Response<BackupRestoreStatusResponse> response,
      Result<BackupRestoreResponse> result) {
    BackupRestoreStatusResponse statusRestoreResponse = response.getBody();
    BackupRestoreResponse restoreResponse = result.getResult();

    BackupRestoreResponse merged = null;
    if (statusRestoreResponse != null) {
      merged = new BackupRestoreResponse();

      merged.setId(statusRestoreResponse.getId());
      merged.setBackend(statusRestoreResponse.getBackend());
      merged.setPath(statusRestoreResponse.getPath());
      merged.setStatus(statusRestoreResponse.getStatus());
      merged.setError(statusRestoreResponse.getError());
      merged.setClassNames(restoreResponse.getClassNames());
    }

    return new Result<>(response.getStatusCode(), merged, response.getErrors());
  }

  @Getter
  @Builder
  private static class BackupRestore {
    @SerializedName("config")
    BackupRestoreConfig config;
    @SerializedName("include")
    String[] include;
    @SerializedName("exclude")
    String[] exclude;
    @SerializedName("overwriteAlias")
    Boolean overwriteAlias;
  }

  @Getter
  @Builder
  public static class BackupRestoreConfig {
    @SerializedName("CPUPercentage")
    Integer cpuPercentage;
    @SerializedName("Bucket")
    String bucket;
    @SerializedName("Path")
    String path;
    @SerializedName("usersOptions")
    RbacRestoreOption usersRestore;
    @SerializedName("rolesOptions")
    RbacRestoreOption rolesRestore;
  }
}
