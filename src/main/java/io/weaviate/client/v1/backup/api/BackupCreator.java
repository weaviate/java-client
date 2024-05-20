package io.weaviate.client.v1.backup.api;

import com.google.gson.annotations.SerializedName;
import io.weaviate.client.v1.backup.model.BackupCreateResponse;
import io.weaviate.client.v1.backup.model.BackupCreateStatusResponse;
import io.weaviate.client.v1.backup.model.CreateStatus;
import lombok.Builder;
import lombok.Getter;
import io.weaviate.client.Config;
import io.weaviate.client.base.BaseClient;
import io.weaviate.client.base.ClientResult;
import io.weaviate.client.base.Response;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.http.HttpClient;

public class BackupCreator extends BaseClient<BackupCreateResponse> implements ClientResult<BackupCreateResponse> {

  private static final long WAIT_INTERVAL = 1000;

  private final BackupCreateStatusGetter statusGetter;
  private String[] includeClassNames;
  private String[] excludeClassNames;
  private String backend;
  private String backupId;
  private BackupCreateConfig config;
  private boolean waitForCompletion;

  public BackupCreator(HttpClient httpClient, Config config, BackupCreateStatusGetter statusGetter) {
    super(httpClient, config);
    this.statusGetter = statusGetter;
  }


  public BackupCreator withIncludeClassNames(String... classNames) {
    this.includeClassNames = classNames;
    return this;
  }

  public BackupCreator withExcludeClassNames(String... classNames) {
    this.excludeClassNames = classNames;
    return this;
  }

  public BackupCreator withBackend(String backend) {
    this.backend = backend;
    return this;
  }

  public BackupCreator withBackupId(String backupId) {
    this.backupId = backupId;
    return this;
  }

  public BackupCreator withConfig(BackupCreateConfig config) {
    this.config = config;
    return this;
  }

  public BackupCreator withWaitForCompletion(boolean waitForCompletion) {
    this.waitForCompletion = waitForCompletion;
    return this;
  }

  @Override
  public Result<BackupCreateResponse> run() {
    BackupCreate payload = BackupCreate.builder()
      .id(backupId)
      .config(config)
      .include(includeClassNames)
      .exclude(excludeClassNames)
      .build();

    if (waitForCompletion) {
      return createAndWaitForCompletion(payload);
    }
    return create(payload);
  }

  private Result<BackupCreateResponse> create(BackupCreate payload) {
    Response<BackupCreateResponse> response = sendPostRequest(path(), payload, BackupCreateResponse.class);
    return new Result<>(response);
  }

  private Result<BackupCreateResponse> createAndWaitForCompletion(BackupCreate payload) {
    Result<BackupCreateResponse> result = create(payload);
    if (result.hasErrors()) {
      return result;
    }

    statusGetter.withBackend(backend).withBackupId(backupId);
    while(true) {
      Response<BackupCreateStatusResponse> statusResponse = statusGetter.statusCreate();
      if (new Result<>(statusResponse).hasErrors()) {
        return merge(statusResponse, result);
      }

      switch (statusResponse.getBody().getStatus()) {
        case CreateStatus.SUCCESS:
        case CreateStatus.FAILED:
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
    return String.format("/backups/%s", backend);
  }

  private Result<BackupCreateResponse> merge(Response<BackupCreateStatusResponse> response, Result<BackupCreateResponse> result) {
    BackupCreateStatusResponse statusCreateResponse = response.getBody();
    BackupCreateResponse createResponse = result.getResult();

    BackupCreateResponse merged = null;
    if (statusCreateResponse != null) {
      merged = new BackupCreateResponse();

      merged.setId(statusCreateResponse.getId());
      merged.setBackend(statusCreateResponse.getBackend());
      merged.setPath(statusCreateResponse.getPath());
      merged.setStatus(statusCreateResponse.getStatus());
      merged.setError(statusCreateResponse.getError());
      merged.setClassNames(createResponse.getClassNames());
    }

    return new Result<>(response.getStatusCode(), merged, response.getErrors());
  }


  @Getter
  @Builder
  private static class BackupCreate {
    String id;
    String[] include;
    String[] exclude;
    BackupCreateConfig config;
  }

  @Getter
  @Builder
  public static class BackupCreateConfig {
    @SerializedName("CPUPercentage")
    Integer cpuPercentage;
    @SerializedName("ChunkSize")
    Integer chunkSize;
    @SerializedName("CompressionLevel")
    String compressionLevel;
  }

  public interface BackupCompression {
    String DEFAULT_COMPRESSION = "DefaultCompression";
    String BEST_SPEED = "BestSpeed";
    String BEST_COMPRESSION = "BestCompression";
  }
}
