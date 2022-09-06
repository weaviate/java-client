package technology.semi.weaviate.client.v1.backup.api;

import lombok.Builder;
import lombok.Getter;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.base.BaseClient;
import technology.semi.weaviate.client.base.ClientResult;
import technology.semi.weaviate.client.base.Response;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.backup.model.BackupCreateResponse;
import technology.semi.weaviate.client.v1.backup.model.BackupCreateStatusResponse;
import technology.semi.weaviate.client.v1.backup.model.CreateStatus;

public class BackupCreator extends BaseClient<BackupCreateResponse> implements ClientResult<BackupCreateResponse> {

  private static final long WAIT_INTERVAL = 1000;

  private final BackupCreateStatusGetter statusGetter;
  private String[] includeClassNames;
  private String[] excludeClassNames;
  private String storageName;
  private String backupId;
  private boolean waitForCompletion;

  public BackupCreator(Config config, BackupCreateStatusGetter statusGetter) {
    super(config);
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

  public BackupCreator withStorageName(String storageName) {
    this.storageName = storageName;
    return this;
  }

  public BackupCreator withBackupId(String backupId) {
    this.backupId = backupId;
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
      .config(BackupCreateConfig.builder().build())
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

    statusGetter.withStorageName(storageName).withBackupId(backupId);
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
    return String.format("/backups/%s", storageName);
  }

  private Result<BackupCreateResponse> merge(Response<BackupCreateStatusResponse> response, Result<BackupCreateResponse> result) {
    BackupCreateStatusResponse statusCreateResponse = response.getBody();
    BackupCreateResponse createResponse = result.getResult();

    BackupCreateResponse merged = null;
    if (statusCreateResponse != null) {
      merged = new BackupCreateResponse();

      merged.setId(statusCreateResponse.getId());
      merged.setStorageName(statusCreateResponse.getStorageName());
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
    BackupCreateConfig config;
    String[] include;
    String[] exclude;
  }

  @Getter
  @Builder
  public static class BackupCreateConfig {
    // TBD
  }
}
