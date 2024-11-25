package io.weaviate.integration.client.backup;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.backup.api.BackupCreator;
import io.weaviate.client.v1.backup.api.BackupRestorer;
import io.weaviate.client.v1.backup.model.BackupCreateResponse;
import io.weaviate.client.v1.backup.model.BackupCreateStatusResponse;
import io.weaviate.client.v1.backup.model.BackupRestoreResponse;
import io.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import io.weaviate.integration.tests.backup.BackupTestSuite;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClientBackupTest {

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
    Config config = new Config("http", compose.getHttpHostAddress());
    client = new WeaviateClient(config);
    testGenerics.createTestSchemaAndData(client);

    backupId = String.format("backup-%s-%s", currentTest.getMethodName().toLowerCase(), rand.nextInt(Integer.MAX_VALUE));
    notExistingBackupId = "not-existing-backup-" + backupId;
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }


  @Test
  public void shouldCreateAndRestoreBackupWithWaiting() {
    Supplier<Result<BackupCreateResponse>> supplierCreateResult = () -> client.backup().creator()
      .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();
    Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = () -> client.backup().createStatusGetter()
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();
    Supplier<Result<Boolean>> supplierDeleteClass = () -> client.schema().classDeleter()
      .withClassName(BackupTestSuite.CLASS_NAME_PIZZA)
      .run();
    Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = () -> client.backup().restorer()
      .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();
    Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = () -> client.backup().restoreStatusGetter()
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();

    BackupTestSuite.testCreateAndRestoreBackupWithWaiting(supplierCreateResult, supplierCreateStatusResult,
      supplierRestoreResult, supplierRestoreStatusResult, supplierDeleteClass, createSupplierGQLOfClass(), backupId);
  }

  @Test
  public void shouldCreateAndRestoreBackupWithoutWaiting() throws InterruptedException {
    Supplier<Result<BackupCreateResponse>> supplierCreateResult = () -> client.backup().creator()
      .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();
    Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = () -> client.backup().createStatusGetter()
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();
    Supplier<Result<Boolean>> supplierDeleteClass = () -> client.schema().classDeleter()
      .withClassName(BackupTestSuite.CLASS_NAME_PIZZA)
      .run();
    Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = () -> client.backup().restorer()
      .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();
    Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = () -> client.backup().restoreStatusGetter()
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();

    BackupTestSuite.testCreateAndRestoreBackupWithoutWaiting(supplierCreateResult, supplierCreateStatusResult,
      supplierRestoreResult, supplierRestoreStatusResult, supplierDeleteClass, createSupplierGQLOfClass(), backupId);
  }

  @Test
  public void shouldCreateAndRestore1Of2Classes() {
    Supplier<Result<BackupCreateResponse>> supplierCreateResult = () -> client.backup().creator()
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();
    Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = () -> client.backup().createStatusGetter()
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();
    Supplier<Result<Boolean>> supplierDeleteClass = () -> client.schema().classDeleter()
      .withClassName(BackupTestSuite.CLASS_NAME_PIZZA)
      .run();
    Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = () -> client.backup().restorer()
      .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();
    Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = () -> client.backup().restoreStatusGetter()
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();

    BackupTestSuite.testCreateAndRestore1Of2Classes(supplierCreateResult, supplierCreateStatusResult,
      supplierRestoreResult, supplierRestoreStatusResult, supplierDeleteClass, createSupplierGQLOfClass(), backupId);
  }

  @Test
  public void shouldListCreatedBackups() {
    List<Supplier<Result<BackupCreateResponse>>> createSuppliers = new ArrayList<Supplier<Result<BackupCreateResponse>>>() {{
      this.add(() -> client.backup().creator()
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId+"-1")
          .withWaitForCompletion(true)
          .run()
      );
      this.add(() -> client.backup().creator()
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId+"-2")
          .withWaitForCompletion(true)
          .run()
      );
    }};

    Supplier<Result<BackupCreateResponse[]>> supplierGetResult = () -> client.backup().getter().withBackend(BackupTestSuite.BACKEND).run();

    BackupTestSuite.testListExistingBackups(createSuppliers, supplierGetResult);
  }

  @Test
  public void shouldFailOnCreateBackupOnNotExistingBackend() {
    Supplier<Result<BackupCreateResponse>> supplierCreateResult = () -> client.backup().creator()
      .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
      .withBackend(BackupTestSuite.NOT_EXISTING_BACKEND)
      .withBackupId(backupId)
      .run();

    BackupTestSuite.testFailOnCreateBackupOnNotExistingBackend(supplierCreateResult);
  }

  @Test
  public void shouldFailOnCreateBackupStatusOnNotExistingBackend() {
    Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = () -> client.backup().createStatusGetter()
      .withBackend(BackupTestSuite.NOT_EXISTING_BACKEND)
      .withBackupId(backupId)
      .run();

    BackupTestSuite.testFailOnCreateBackupStatusOnNotExistingBackend(supplierCreateStatusResult);
  }

  @Test
  public void shouldFailOnRestoreBackupFromNotExistingBackend() {
    Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = () -> client.backup().restorer()
      .withIncludeClassNames(BackupTestSuite.NOT_EXISTING_CLASS_NAME)
      .withBackend(BackupTestSuite.NOT_EXISTING_BACKEND)
      .withBackupId(backupId)
      .run();

    BackupTestSuite.testFailOnRestoreBackupFromNotExistingBackend(supplierRestoreResult);
  }

  @Test
  public void shouldFailOnCreateBackupForNotExistingClass() {
    Supplier<Result<BackupCreateResponse>> supplierCreateResult = () -> client.backup().creator()
      .withIncludeClassNames(BackupTestSuite.NOT_EXISTING_CLASS_NAME)
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();

    BackupTestSuite.testFailOnCreateBackupForNotExistingClass(supplierCreateResult);
  }

  @Test
  public void shouldFailOnRestoreBackupForExistingClass() {
    Supplier<Result<BackupCreateResponse>> supplierCreateResult = () -> client.backup().creator()
      .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();
    Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = () -> client.backup().restorer()
      .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    BackupTestSuite.testFailOnRestoreBackupForExistingClass(supplierCreateResult, supplierRestoreResult, backupId);
  }

  @Test
  public void shouldFailOnCreateOfExistingBackup() {
    Supplier<Result<BackupCreateResponse>> supplierCreateResult = () -> client.backup().creator()
      .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    BackupTestSuite.testFailOnCreateOfExistingBackup(supplierCreateResult, backupId);
  }

  @Test
  public void shouldFailOnCreateStatusOfNotExistingBackup() {
    Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = () -> client.backup().createStatusGetter()
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(notExistingBackupId)
      .run();

    BackupTestSuite.testFailOnCreateStatusOfNotExistingBackup(supplierCreateStatusResult, notExistingBackupId);
  }

  @Test
  public void shouldFailOnRestoreOfNotExistingBackup() {
    Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = () -> client.backup().restorer()
      .withIncludeClassNames(BackupTestSuite.NOT_EXISTING_CLASS_NAME)
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(notExistingBackupId)
      .run();

    BackupTestSuite.testFailOnRestoreOfNotExistingBackup(supplierRestoreResult, notExistingBackupId);
  }

  @Test
  public void shouldFailOnRestoreBackupStatusOfNotStartedRestore() {
    Supplier<Result<BackupCreateResponse>> supplierCreateResult = () -> client.backup().creator()
      .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();
    Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = () -> client.backup().restoreStatusGetter()
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();

    BackupTestSuite.testFailOnRestoreBackupStatusOfNotStartedRestore(supplierCreateResult, supplierRestoreStatusResult, backupId);
  }

  @Test
  public void shouldFailOnCreateBackupForBothIncludeAndExcludeClasses() {
    Supplier<Result<BackupCreateResponse>> supplierCreateResult = () -> client.backup().creator()
      .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
      .withExcludeClassNames(BackupTestSuite.CLASS_NAME_SOUP)
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    BackupTestSuite.testFailOnCreateBackupForBothIncludeAndExcludeClasses(supplierCreateResult);
  }

  @Test
  public void shouldFailOnRestoreBackupForBothIncludeAndExcludeClasses() {
    Supplier<Result<BackupCreateResponse>> supplierCreateResult = () -> client.backup().creator()
      .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA, BackupTestSuite.CLASS_NAME_SOUP)
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();
    Supplier<Result<Boolean>> supplierDeleteClass = () -> client.schema().classDeleter()
      .withClassName(BackupTestSuite.CLASS_NAME_PIZZA)
      .run();
    Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = () -> client.backup().restorer()
      .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
      .withExcludeClassNames(BackupTestSuite.CLASS_NAME_SOUP)
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();

    BackupTestSuite.testFailOnRestoreBackupForBothIncludeAndExcludeClasses(supplierCreateResult, supplierRestoreResult,
      supplierDeleteClass);
  }

  @Test
  public void shouldCreateAndRestoreBackupWithWaitingWithConfig() {
    // config with too high value
    Supplier<Result<BackupCreateResponse>> supplierCreateInvConfigResult = () -> {
      BackupCreator.BackupCreateConfig config = BackupCreator.BackupCreateConfig.builder()
        .cpuPercentage(801)
        .build();

      return client.backup().creator()
        .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
        .withBackend(BackupTestSuite.BACKEND)
        .withBackupId(backupId)
        .withConfig(config)
        .withWaitForCompletion(true)
        .run();
    };
    // valid config
    Supplier<Result<BackupCreateResponse>> supplierCreateResult = () -> {
      BackupCreator.BackupCreateConfig config = BackupCreator.BackupCreateConfig.builder()
        .cpuPercentage(80)
        .chunkSize(512)
        .compressionLevel(BackupCreator.BackupCompression.BEST_SPEED)
        .build();

      return client.backup().creator()
        .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
        .withBackend(BackupTestSuite.BACKEND)
        .withBackupId(backupId)
        .withConfig(config)
        .withWaitForCompletion(true)
        .run();
    };
    Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = () -> client.backup().createStatusGetter()
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();
    Supplier<Result<Boolean>> supplierDeleteClass = () -> client.schema().classDeleter()
      .withClassName(BackupTestSuite.CLASS_NAME_PIZZA)
      .run();
    // config with too high value
    Supplier<Result<BackupRestoreResponse>> supplierRestoreInvConfigResult = () -> {
      BackupRestorer.BackupRestoreConfig restoreConfig = BackupRestorer.BackupRestoreConfig.builder()
        .cpuPercentage(90)
        .build();

      return client.backup().restorer()
        .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
        .withBackend(BackupTestSuite.BACKEND)
        .withBackupId(backupId)
        .withConfig(restoreConfig)
        .withWaitForCompletion(true)
        .run();
    };
    // valid config
    Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = () -> {
      BackupRestorer.BackupRestoreConfig restoreConfig = BackupRestorer.BackupRestoreConfig.builder()
        .cpuPercentage(70)
        .build();

      return client.backup().restorer()
        .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
        .withBackend(BackupTestSuite.BACKEND)
        .withBackupId(backupId)
        .withConfig(restoreConfig)
        .withWaitForCompletion(true)
        .run();
    };
    Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = () -> client.backup().restoreStatusGetter()
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();

    BackupTestSuite.testCreateAndRestoreBackupWithWaitingWithConfig(supplierCreateInvConfigResult, supplierCreateResult,
      supplierCreateStatusResult, supplierRestoreInvConfigResult, supplierRestoreResult, supplierRestoreStatusResult,
      supplierDeleteClass, createSupplierGQLOfClass(), backupId);
  }

  @Test
  public void shouldCancelBackup() {
    Supplier<Result<BackupCreateResponse>> supplierCreateResult = () -> client.backup().creator()
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(false) // this will allow us to "intercept" the backup in progress
      .run();
    Supplier<Result<Void>> supplierCancelResult = () -> client.backup().canceler()
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();
    Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = () -> client.backup().createStatusGetter()
      .withBackend(BackupTestSuite.BACKEND)
      .withBackupId(backupId)
      .run();

    BackupTestSuite.testCancelBackup(supplierCreateResult, supplierCancelResult, supplierCreateStatusResult);
  }

  @NotNull
  private Function<String, Result<GraphQLResponse>> createSupplierGQLOfClass() {
    return (String className) -> client.graphQL().get()
      .withClassName(className)
      .withFields(Field.builder().name("name").build())
      .run();
  }
}
