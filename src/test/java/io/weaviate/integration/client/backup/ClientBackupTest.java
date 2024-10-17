package io.weaviate.integration.client.backup;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.backup.api.BackupCreateStatusGetter;
import io.weaviate.client.v1.backup.api.BackupCreator;
import io.weaviate.client.v1.backup.api.BackupRestoreStatusGetter;
import io.weaviate.client.v1.backup.api.BackupRestorer;
import io.weaviate.client.v1.backup.model.Backend;
import io.weaviate.client.v1.backup.model.BackupCreateResponse;
import io.weaviate.client.v1.backup.model.BackupCreateStatusResponse;
import io.weaviate.client.v1.backup.model.BackupRestoreResponse;
import io.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;
import io.weaviate.client.v1.backup.model.CreateStatus;
import io.weaviate.client.v1.backup.model.RestoreStatus;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;
import static org.assertj.core.api.InstanceOfAssertFactories.CHAR_SEQUENCE;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class ClientBackupTest {

  private static final String DOCKER_COMPOSE_BACKUPS_DIR = "/tmp/backups";
  private static final String CLASS_NAME_PIZZA = "Pizza";
  private static final String CLASS_NAME_SOUP = "Soup";
  private static final String NOT_EXISTING_CLASS_NAME = "not-existing-class";
  private static final String BACKEND = Backend.FILESYSTEM;
  private static final String NOT_EXISTING_BACKEND = "not-existing-backend";

  private String backupId;
  private String notExistingBackupId;
  private WeaviateClient client;

  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();
  private final static Random rand = new Random();

  @Rule
  public TestName currentTest = new TestName();

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    String httpHost = compose.getHttpHostAddress();
    Config config = new Config("http", httpHost);

    backupId = String.format("backup-%s-%s", currentTest.getMethodName().toLowerCase(), rand.nextInt(Integer.MAX_VALUE));
    notExistingBackupId = "not-existing-backup-" + backupId;

    client = new WeaviateClient(config);
    testGenerics.createTestSchemaAndData(client);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }


  @Test
  public void shouldCreateAndRestoreBackupWithWaiting() {
    assertThatAllPizzasExist();

    // Create backup
    Result<BackupCreateResponse> createResult = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(createResult.getError()).as("create backup").isNull();
    assertThat(createResult.getResult()).isNotNull()
      .returns(backupId, BackupCreateResponse::getId)
      .returns(new String[]{ CLASS_NAME_PIZZA }, BackupCreateResponse::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateResponse::getPath)
      .returns(BACKEND, BackupCreateResponse::getBackend)
      .returns(CreateStatus.SUCCESS, BackupCreateResponse::getStatus)
      .returns(null, BackupCreateResponse::getError);

    assertThatAllPizzasExist();

    // Check backup status
    Result<BackupCreateStatusResponse> createStatusResult = client.backup().createStatusGetter()
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(createStatusResult.getError()).as("check backup creation status").isNull();
    assertThat(createStatusResult.getResult()).isNotNull()
      .returns(backupId, BackupCreateStatusResponse::getId)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateStatusResponse::getPath)
      .returns(BACKEND, BackupCreateStatusResponse::getBackend)
      .returns(CreateStatus.SUCCESS, BackupCreateStatusResponse::getStatus)
      .returns(null, BackupCreateStatusResponse::getError);

    // Remove existing class
    Result<Boolean> delete = client.schema().classDeleter()
      .withClassName(CLASS_NAME_PIZZA)
      .run();

    assertThat(delete.getError()).as("drop Pizza collection").isNull();
    assertThat(delete.getResult()).isTrue();

    // Restore backup
    Result<BackupRestoreResponse> restoreResult = client.backup().restorer()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(restoreResult.getError()).as("restore from backup").isNull();
    assertThat(restoreResult.getResult()).isNotNull()
      .returns(backupId, BackupRestoreResponse::getId)
      .returns(new String[]{ CLASS_NAME_PIZZA }, BackupRestoreResponse::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreResponse::getPath)
      .returns(BACKEND, BackupRestoreResponse::getBackend)
      .returns(RestoreStatus.SUCCESS, BackupRestoreResponse::getStatus)
      .returns(null, BackupRestoreResponse::getError);

    assertThatAllPizzasExist();

    // Check restore backup
    Result<BackupRestoreStatusResponse> restoreStatusResult = client.backup().restoreStatusGetter()
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(restoreStatusResult.getError()).as("get restore status").isNull();
    assertThat(restoreStatusResult.getResult()).isNotNull()
      .returns(backupId, BackupRestoreStatusResponse::getId)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreStatusResponse::getPath)
      .returns(BACKEND, BackupRestoreStatusResponse::getBackend)
      .returns(RestoreStatus.SUCCESS, BackupRestoreStatusResponse::getStatus)
      .returns(null, BackupRestoreStatusResponse::getError);
  }

  @Test
  public void shouldCreateAndRestoreBackupWithoutWaiting() throws InterruptedException {
    assertThatAllPizzasExist();

    // Start creating backup
    Result<BackupCreateResponse> createResult = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(createResult.getError()).as("create backup").isNull();
    assertThat(createResult.getResult()).isNotNull()
      .returns(backupId, BackupCreateResponse::getId)
      .returns(new String[]{ CLASS_NAME_PIZZA }, BackupCreateResponse::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateResponse::getPath)
      .returns(BACKEND, BackupCreateResponse::getBackend)
      .returns(CreateStatus.STARTED, BackupCreateResponse::getStatus)
      .returns(null, BackupCreateResponse::getError);

    // Wait until created
    BackupCreateStatusGetter createStatusGetter = client.backup().createStatusGetter()
      .withBackend(BACKEND)
      .withBackupId(backupId);

    Result<BackupCreateStatusResponse> createStatusResult;
    while (true) {
      createStatusResult = createStatusGetter.run();

      assertThat(createStatusResult.getError()).as("check backup creation status").isNull();
      assertThat(createStatusResult.getResult()).isNotNull()
        .returns(backupId, BackupCreateStatusResponse::getId)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateStatusResponse::getPath)
        .returns(BACKEND, BackupCreateStatusResponse::getBackend)
        .returns(null, BackupCreateStatusResponse::getError)
        .extracting(BackupCreateStatusResponse::getStatus).isIn(CreateStatus.STARTED, CreateStatus.TRANSFERRING,
          CreateStatus.TRANSFERRED, CreateStatus.SUCCESS
        );

      if (CreateStatus.SUCCESS.equals(createStatusResult.getResult().getStatus())) {
        break;
      }
      Thread.sleep(100);
    }

    assertThatAllPizzasExist();

    // Remove existing class
    Result<Boolean> delete = client.schema().classDeleter()
      .withClassName(CLASS_NAME_PIZZA)
      .run();

    assertThat(delete.getError()).as("drop Pizza collection").isNull();
    assertThat(delete.getResult()).isTrue();

    // Start restoring backup
    Result<BackupRestoreResponse> restoreResult = client.backup().restorer()
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(restoreResult.getError()).as("restore from backup").isNull();
    assertThat(restoreResult.getResult()).isNotNull()
      .returns(backupId, BackupRestoreResponse::getId)
      .returns(new String[]{ CLASS_NAME_PIZZA }, BackupRestoreResponse::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreResponse::getPath)
      .returns(BACKEND, BackupRestoreResponse::getBackend)
      .returns(RestoreStatus.STARTED, BackupRestoreResponse::getStatus)
      .returns(null, BackupRestoreResponse::getError);

    // Wait until restored
    BackupRestoreStatusGetter restoreStatusGetter = client.backup().restoreStatusGetter()
      .withBackend(BACKEND)
      .withBackupId(backupId);

    Result<BackupRestoreStatusResponse> restoreStatusResult;
    while (true) {
      restoreStatusResult = restoreStatusGetter.run();

      assertThat(restoreStatusResult.getError()).as("get restore status").isNull();
      assertThat(restoreStatusResult.getResult()).isNotNull()
        .returns(backupId, BackupRestoreStatusResponse::getId)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreStatusResponse::getPath)
        .returns(BACKEND, BackupRestoreStatusResponse::getBackend)
        .returns(null, BackupRestoreStatusResponse::getError)
        .extracting(BackupRestoreStatusResponse::getStatus).isIn(RestoreStatus.STARTED, RestoreStatus.TRANSFERRING,
          RestoreStatus.TRANSFERRED, RestoreStatus.SUCCESS
        );

      if (RestoreStatus.SUCCESS.equals(restoreStatusResult.getResult().getStatus())) {
        break;
      }
      Thread.sleep(100);
    }

    assertThatAllPizzasExist();
  }

  @Test
  public void shouldCreateAndRestore1Of2Classes() {
    assertThatAllPizzasExist();
    assertThatAllSoupsExist();

    // Create backup for all existing classes (2)
    Result<BackupCreateResponse> createResult = client.backup().creator()
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(createResult.hasErrors()).isFalse();
    assertThat(createResult.getResult()).isNotNull()
      .returns(backupId, BackupCreateResponse::getId)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateResponse::getPath)
      .returns(BACKEND, BackupCreateResponse::getBackend)
      .returns(CreateStatus.SUCCESS, BackupCreateResponse::getStatus)
      .returns(null, BackupCreateResponse::getError)
      .extracting(BackupCreateResponse::getClassNames).asInstanceOf(ARRAY)
      .containsExactlyInAnyOrder(CLASS_NAME_PIZZA, CLASS_NAME_SOUP);

    assertThatAllPizzasExist();
    assertThatAllSoupsExist();

    // Check backup status
    Result<BackupCreateStatusResponse> createStatusResult = client.backup().createStatusGetter()
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(createStatusResult.hasErrors()).isFalse();
    assertThat(createStatusResult.getResult()).isNotNull()
      .returns(backupId, BackupCreateStatusResponse::getId)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateStatusResponse::getPath)
      .returns(BACKEND, BackupCreateStatusResponse::getBackend)
      .returns(CreateStatus.SUCCESS, BackupCreateStatusResponse::getStatus)
      .returns(null, BackupCreateStatusResponse::getError);

    // Remove existing class
    Result<Boolean> delete = client.schema().classDeleter()
      .withClassName(CLASS_NAME_PIZZA)
      .run();

    assertThat(delete.hasErrors()).isFalse();
    assertThat(delete.getResult()).isTrue();

    // Restore backup
    Result<BackupRestoreResponse> restoreResult = client.backup().restorer()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(restoreResult.hasErrors()).isFalse();
    assertThat(restoreResult.getResult()).isNotNull()
      .returns(backupId, BackupRestoreResponse::getId)
      .returns(new String[]{ CLASS_NAME_PIZZA }, BackupRestoreResponse::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreResponse::getPath)
      .returns(BACKEND, BackupRestoreResponse::getBackend)
      .returns(RestoreStatus.SUCCESS, BackupRestoreResponse::getStatus)
      .returns(null, BackupRestoreResponse::getError);

    assertThatAllPizzasExist();
    assertThatAllSoupsExist();

    // Check restore backup
    Result<BackupRestoreStatusResponse> restoreStatusResult = client.backup().restoreStatusGetter()
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(restoreStatusResult.hasErrors()).isFalse();
    assertThat(restoreStatusResult.getResult()).isNotNull()
      .returns(backupId, BackupRestoreStatusResponse::getId)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreStatusResponse::getPath)
      .returns(BACKEND, BackupRestoreStatusResponse::getBackend)
      .returns(RestoreStatus.SUCCESS, BackupRestoreStatusResponse::getStatus)
      .returns(null, BackupRestoreStatusResponse::getError);
  }

  @Test
  public void shouldFailOnCreateBackupOnNotExistingBackend() {
    Result<BackupCreateResponse> createResult = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(NOT_EXISTING_BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(createResult.hasErrors()).isTrue();
    assertThat(createResult.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_BACKEND);
  }

  @Test
  public void shouldFailOnCreateBackupStatusOnNotExistingBackend() {
    Result<BackupCreateStatusResponse> createStatusResult = client.backup().createStatusGetter()
      .withBackend(NOT_EXISTING_BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(createStatusResult.hasErrors()).isTrue();
    assertThat(createStatusResult.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_BACKEND);
  }

  @Test
  public void shouldFailOnRestoreBackupFromNotExistingBackend() {
    Result<BackupRestoreResponse> restoreResult = client.backup().restorer()
      .withIncludeClassNames(NOT_EXISTING_CLASS_NAME)
      .withBackend(NOT_EXISTING_BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(restoreResult.hasErrors()).isTrue();
    assertThat(restoreResult.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_BACKEND);
  }

  @Test
  public void shouldFailOnCreateBackupForNotExistingClass() {
    Result<BackupCreateResponse> createResult = client.backup().creator()
      .withIncludeClassNames(NOT_EXISTING_CLASS_NAME)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(createResult.hasErrors()).isTrue();
    assertThat(createResult.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_CLASS_NAME);
  }

  @Test
  public void shouldFailOnRestoreBackupForExistingClass() {
    Result<BackupCreateResponse> createResult = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(createResult.hasErrors()).isFalse();

    Result<BackupRestoreResponse> restoreResult = client.backup().restorer()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(restoreResult.hasErrors()).isFalse();
    assertThat(restoreResult.getResult()).isNotNull()
      .returns(backupId, BackupRestoreResponse::getId)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreResponse::getPath)
      .returns(BACKEND, BackupRestoreResponse::getBackend)
      .returns(RestoreStatus.FAILED, BackupRestoreResponse::getStatus)
      .returns("could not restore classes: [\"Pizza\": class name Pizza already exists]", BackupRestoreResponse::getError);
  }

  @Test
  public void shouldFailOnCreateOfExistingBackup() {
    Result<BackupCreateResponse> createResult = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(createResult.hasErrors()).isFalse();

    Result<BackupCreateResponse> createResultAgain = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(createResultAgain.hasErrors()).isTrue();
    assertThat(createResultAgain.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(backupId);
  }

  @Test
  public void shouldFailOnCreateStatusOfNotExistingBackup() {
    Result<BackupCreateStatusResponse> createStatusResult = client.backup().createStatusGetter()
      .withBackend(BACKEND)
      .withBackupId(notExistingBackupId)
      .run();

    assertThat(createStatusResult.hasErrors()).isTrue();
    assertThat(createStatusResult.getError()).isNotNull()
      .returns(404, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(notExistingBackupId);
  }

  @Test
  public void shouldFailOnRestoreOfNotExistingBackup() {
    Result<BackupRestoreResponse> restoreResult = client.backup().restorer()
      .withIncludeClassNames(NOT_EXISTING_CLASS_NAME)
      .withBackend(BACKEND)
      .withBackupId(notExistingBackupId)
      .run();

    assertThat(restoreResult.hasErrors()).isTrue();
    assertThat(restoreResult.getError()).isNotNull()
      .returns(404, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(notExistingBackupId);
  }

  @Test
  public void shouldFailOnRestoreBackupStatusOfNotStartedRestore() {
    Result<BackupCreateResponse> createResult = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(createResult.hasErrors()).isFalse();

    Result<BackupRestoreStatusResponse> restoreStatusResult = client.backup().restoreStatusGetter()
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(restoreStatusResult.hasErrors()).isTrue();
    assertThat(restoreStatusResult.getError()).isNotNull()
      .returns(404, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(backupId);
  }

  @Test
  public void shouldFailOnCreateBackupForBothIncludeAndExcludeClasses() {
    Result<BackupCreateResponse> createResult = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withExcludeClassNames(CLASS_NAME_SOUP)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(createResult.hasErrors()).isTrue();
    assertThat(createResult.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains("include").contains("exclude");
  }

  @Test
  public void shouldFailOnRestoreBackupForBothIncludeAndExcludeClasses() {
    // Create backup
    Result<BackupCreateResponse> createResult = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA, CLASS_NAME_SOUP)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(createResult.hasErrors()).isFalse();

    // Remove existing class
    Result<Boolean> delete = client.schema().classDeleter()
      .withClassName(CLASS_NAME_PIZZA)
      .run();

    assertThat(delete.hasErrors()).isFalse();

    // Restore
    Result<BackupRestoreResponse> restoreResult = client.backup().restorer()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withExcludeClassNames(CLASS_NAME_SOUP)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(restoreResult.hasErrors()).isTrue();
    assertThat(restoreResult.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains("include").contains("exclude");
  }

  @Test
  public void shouldCreateAndRestoreBackupWithWaitingWithConfig() {
    assertThatAllPizzasExist();

    // Try to create with too high value
    BackupCreator.BackupCreateConfig config = BackupCreator.BackupCreateConfig.builder()
      .cpuPercentage(801)
      .build();

    Result<BackupCreateResponse> createResult = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withConfig(config)
      .withWaitForCompletion(true)
      .run();

    assertThat(createResult).isNotNull()
      .extracting(Result::getError).isNotNull()
      .extracting(WeaviateError::getMessages)
      .satisfies(errors -> {
        assertThat(errors.stream().filter(m -> m.getMessage().contains("CPUPercentage")).count()).isGreaterThanOrEqualTo(1);
      });

    // Pass backup config
    config = BackupCreator.BackupCreateConfig.builder()
      .cpuPercentage(80)
      .chunkSize(512)
      .compressionLevel(BackupCreator.BackupCompression.BEST_SPEED)
      .build();

    // Create backup
    createResult = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withConfig(config)
      .withWaitForCompletion(true)
      .run();

    assertThat(createResult.hasErrors()).isFalse();
    assertThat(createResult.getResult()).isNotNull()
      .returns(backupId, BackupCreateResponse::getId)
      .returns(new String[]{ CLASS_NAME_PIZZA }, BackupCreateResponse::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateResponse::getPath)
      .returns(BACKEND, BackupCreateResponse::getBackend)
      .returns(CreateStatus.SUCCESS, BackupCreateResponse::getStatus)
      .returns(null, BackupCreateResponse::getError);

    assertThatAllPizzasExist();

    // Check backup status
    Result<BackupCreateStatusResponse> createStatusResult = client.backup().createStatusGetter()
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(createStatusResult.hasErrors()).isFalse();
    assertThat(createStatusResult.getResult()).isNotNull()
      .returns(backupId, BackupCreateStatusResponse::getId)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateStatusResponse::getPath)
      .returns(BACKEND, BackupCreateStatusResponse::getBackend)
      .returns(CreateStatus.SUCCESS, BackupCreateStatusResponse::getStatus)
      .returns(null, BackupCreateStatusResponse::getError);

    // Remove existing class
    Result<Boolean> delete = client.schema().classDeleter()
      .withClassName(CLASS_NAME_PIZZA)
      .run();

    assertThat(delete.hasErrors()).isFalse();
    assertThat(delete.getResult()).isTrue();


    // Try to restore with bad restore config
    BackupRestorer.BackupRestoreConfig restoreConfig = BackupRestorer.BackupRestoreConfig.builder()
      .cpuPercentage(90)
      .build();

    Result<BackupRestoreResponse> restoreResult = client.backup().restorer()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withConfig(restoreConfig)
      .withWaitForCompletion(true)
      .run();

    assertThat(restoreResult).isNotNull()
      .extracting(Result::getError).isNotNull()
      .extracting(WeaviateError::getMessages).isNotNull()
      .satisfies(errors -> assertThat(errors.stream().filter(m -> m.getMessage().contains("CPUPercentage")).count()).isGreaterThanOrEqualTo(1));

    restoreConfig = BackupRestorer.BackupRestoreConfig.builder()
      .cpuPercentage(70)
      .build();

    // Restore backup
    restoreResult = client.backup().restorer()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withConfig(restoreConfig)
      .withWaitForCompletion(true)
      .run();

    assertThat(restoreResult.hasErrors()).isFalse();
    assertThat(restoreResult.getResult()).isNotNull()
      .returns(backupId, BackupRestoreResponse::getId)
      .returns(new String[]{ CLASS_NAME_PIZZA }, BackupRestoreResponse::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreResponse::getPath)
      .returns(BACKEND, BackupRestoreResponse::getBackend)
      .returns(RestoreStatus.SUCCESS, BackupRestoreResponse::getStatus)
      .returns(null, BackupRestoreResponse::getError);

    assertThatAllPizzasExist();

    // Check restore backup
    Result<BackupRestoreStatusResponse> restoreStatusResult = client.backup().restoreStatusGetter()
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(restoreStatusResult.hasErrors()).isFalse();
    assertThat(restoreStatusResult.getResult()).isNotNull()
      .returns(backupId, BackupRestoreStatusResponse::getId)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreStatusResponse::getPath)
      .returns(BACKEND, BackupRestoreStatusResponse::getBackend)
      .returns(RestoreStatus.SUCCESS, BackupRestoreStatusResponse::getStatus)
      .returns(null, BackupRestoreStatusResponse::getError);
  }

  @Test
  public void shouldCancelBackup() throws InterruptedException {
    Result<BackupCreateResponse> createResult = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(false) // this will allow us to "intercept" the backup in progress
      .run();
    assertThat(createResult.getError()).as("start backup").isNull();

    Result<Void> cancelResult = client.backup().canceler()
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .run();
    assertThat(cancelResult.getError()).as("cancel backup").isNull();

    waitForCreateStatus(CreateStatus.CANCELED);
  }

  private void assertThatAllPizzasExist() {
    assertThatAllFoodObjectsExist("Pizza", "Quattro Formaggi", "Frutti di Mare", "Hawaii", "Doener");
  }

  private void assertThatAllSoupsExist() {
    assertThatAllFoodObjectsExist("Soup", "ChickenSoup", "Beautiful");
  }

  private void assertThatAllFoodObjectsExist(String className, String... names) {
    Result<GraphQLResponse> result = client.graphQL().get()
      .withClassName(className)
      .withFields(Field.builder().name("name").build())
      .run();

    assertThat(result.hasErrors()).isFalse();
    assertThat(result.getResult()).isNotNull()
      .extracting(GraphQLResponse::getData).isInstanceOf(Map.class)
      .extracting(data -> ((Map<?, ?>) data).get("Get")).isInstanceOf(Map.class)
      .extracting(get -> ((Map<?, ?>) get).get(className)).asList()
      .hasSize(names.length).hasOnlyElementsOfType(Map.class)
      .extracting(pizza -> ((Map<?, ?>) pizza).get("name")).hasOnlyElementsOfType(String.class)
      .extracting(name -> (String) name)
      .containsExactlyInAnyOrder(names);
  }

  /**
   * Periodically polls backup creation status until it reaches the desired ({@code want}) state or the deadline expires.
   *
   * <br>Interval: 100ms
   * <br>Timeout: 5s
   */
  private void waitForCreateStatus(String want) {
    final int MAX_RETRIES = 5_000 / 100;
    AtomicReference<String> status = new AtomicReference<>("");

    Callable<Boolean> statusCheck = new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        Result<BackupCreateStatusResponse> check = client.backup().createStatusGetter().withBackupId(backupId).withBackend(BACKEND).run();
        String current = check.getResult().getStatus();
        status.set(current);
        return current.equalsIgnoreCase(want);
      }
    };

    try {
      int retried = 0;
      do {
        if (statusCheck.call()) {
          return;
        }
        retried++;
        Thread.sleep(100);
      } while (retried < MAX_RETRIES);
    } catch (Exception ignored) {
    }
    fail(String.format("after 5s create status: want=%s, got=%s", want, status.get()));
  }
}
