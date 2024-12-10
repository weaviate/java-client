package io.weaviate.integration.client.async.backup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.async.backup.api.BackupCanceler;
import io.weaviate.client.v1.async.backup.api.BackupCreateStatusGetter;
import io.weaviate.client.v1.async.backup.api.BackupCreator;
import io.weaviate.client.v1.async.backup.api.BackupGetter;
import io.weaviate.client.v1.async.backup.api.BackupRestoreStatusGetter;
import io.weaviate.client.v1.async.backup.api.BackupRestorer;
import io.weaviate.client.v1.backup.model.BackupCreateResponse;
import io.weaviate.client.v1.backup.model.BackupCreateStatusResponse;
import io.weaviate.client.v1.backup.model.BackupRestoreResponse;
import io.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import io.weaviate.integration.tests.backup.BackupTestSuite;

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
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateResponse>> supplierCreateResult = createSupplierCreate(
        asyncClient, creator -> creator
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withWaitForCompletion(true)
      );
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
        asyncClient, createStatusGetter -> createStatusGetter
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
        asyncClient, restorer -> restorer
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withWaitForCompletion(true)
      );
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = createSupplierRestoreStatus(
        asyncClient, restoreStatusGetter -> restoreStatusGetter
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );

      BackupTestSuite.testCreateAndRestoreBackupWithWaiting(supplierCreateResult, supplierCreateStatusResult,
        supplierRestoreResult, supplierRestoreStatusResult,
        createSupplierDeletePizza(), createSupplierGQLOfClass(), backupId);
    }
  }

  @Test
  public void shouldCreateAndRestoreBackupWithoutWaiting() throws InterruptedException {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateResponse>> supplierCreateResult = createSupplierCreate(
        asyncClient, creator -> creator
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
        asyncClient, createStatusGetter -> createStatusGetter
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
        asyncClient, restorer -> restorer
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = createSupplierRestoreStatus(
        asyncClient, restoreStatusGetter -> restoreStatusGetter
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );

      BackupTestSuite.testCreateAndRestoreBackupWithoutWaiting(supplierCreateResult, supplierCreateStatusResult,
        supplierRestoreResult, supplierRestoreStatusResult,
        createSupplierDeletePizza(), createSupplierGQLOfClass(), backupId);
    }
  }

  @Test
  public void shouldCreateAndRestoreBackupWithDynamicLocation() throws InterruptedException {
    String bucket = "test-bucket"; // irrelevant for "filesystem" backend, here only to illustrate
    String path = "/custom/backup/location";

    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateResponse>> supplierCreateResult = createSupplierCreate(
        asyncClient, creator -> creator
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withConfig(BackupCreator.BackupCreateConfig.builder().bucket(bucket).path(path).build())
      );
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
        asyncClient, createStatusGetter -> createStatusGetter
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withBucket(bucket)
          .withPath(path)
      );
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
        asyncClient, restorer -> restorer
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withConfig(BackupRestorer.BackupRestoreConfig.builder().bucket(bucket).path(path).build())
      );
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = createSupplierRestoreStatus(
        asyncClient, restoreStatusGetter -> restoreStatusGetter
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withBucket(bucket)
          .withPath(path)
      );

      BackupTestSuite.testCreateWithDynamicLocation(supplierCreateResult, supplierCreateStatusResult,
        supplierRestoreResult, supplierRestoreStatusResult,
        createSupplierDeletePizza(), createSupplierGQLOfClass(), backupId, bucket, path);
    }
  }

  @Test
  public void shouldCreateAndRestore1Of2Classes() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateResponse>> supplierCreateResult = createSupplierCreate(
        asyncClient, creator -> creator
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withWaitForCompletion(true)
      );
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
        asyncClient, createStatusGetter -> createStatusGetter
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
        asyncClient, restorer -> restorer
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withWaitForCompletion(true)
      );
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = createSupplierRestoreStatus(
        asyncClient, restoreStatusGetter -> restoreStatusGetter
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );

      BackupTestSuite.testCreateAndRestore1Of2Classes(supplierCreateResult, supplierCreateStatusResult,
        supplierRestoreResult, supplierRestoreStatusResult,
        createSupplierDeletePizza(), createSupplierGQLOfClass(), backupId);
    }
  }

  @Test
  public void shouldListCreatedBackups() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      List<Supplier<Result<BackupCreateResponse>>> createSuppliers = new ArrayList<Supplier<Result<BackupCreateResponse>>>() {{
        this.add(createSupplierCreate(
          asyncClient, creator -> creator
            .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
            .withBackend(BackupTestSuite.BACKEND)
            .withBackupId(backupId+"-1")
            .withWaitForCompletion(true)
        ));
        this.add(createSupplierCreate(
          asyncClient, creator -> creator
            .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
            .withBackend(BackupTestSuite.BACKEND)
            .withBackupId(backupId+"-2")
            .withWaitForCompletion(true)
        ));
      }};

      Supplier<Result<BackupCreateResponse[]>> supplierGetResult = createSupplierGet(
        asyncClient, creator -> creator
          .withBackend(BackupTestSuite.BACKEND)
      );

      BackupTestSuite.testListExistingBackups(createSuppliers, supplierGetResult);
    }
  }

  @Test
  public void shouldFailOnCreateBackupOnNotExistingBackend() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateResponse>> supplierCreateResult = createSupplierCreate(
        asyncClient, creator -> creator
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withBackend(BackupTestSuite.NOT_EXISTING_BACKEND)
          .withBackupId(backupId)
      );

      BackupTestSuite.testFailOnCreateBackupOnNotExistingBackend(supplierCreateResult);
    }
  }

  @Test
  public void shouldFailOnCreateBackupStatusOnNotExistingBackend() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
        asyncClient, createStatusGetter -> createStatusGetter
          .withBackend(BackupTestSuite.NOT_EXISTING_BACKEND)
          .withBackupId(backupId)
      );

      BackupTestSuite.testFailOnCreateBackupStatusOnNotExistingBackend(supplierCreateStatusResult);
    }
  }

  @Test
  public void shouldFailOnRestoreBackupFromNotExistingBackend() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
        asyncClient, restorer -> restorer
          .withIncludeClassNames(BackupTestSuite.NOT_EXISTING_CLASS_NAME)
          .withBackend(BackupTestSuite.NOT_EXISTING_BACKEND)
          .withBackupId(backupId)
      );

      BackupTestSuite.testFailOnRestoreBackupFromNotExistingBackend(supplierRestoreResult);
    }
  }

  @Test
  public void shouldFailOnCreateBackupForNotExistingClass() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateResponse>> supplierCreateResult = createSupplierCreate(
        asyncClient, creator -> creator
          .withIncludeClassNames(BackupTestSuite.NOT_EXISTING_CLASS_NAME)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );

      BackupTestSuite.testFailOnCreateBackupForNotExistingClass(supplierCreateResult);
    }
  }

  @Test
  public void shouldFailOnRestoreBackupForExistingClass() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateResponse>> supplierCreateResult = createSupplierCreate(
        asyncClient, creator -> creator
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withWaitForCompletion(true)
      );
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
        asyncClient, restorer -> restorer
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withWaitForCompletion(true)
      );

      BackupTestSuite.testFailOnRestoreBackupForExistingClass(supplierCreateResult, supplierRestoreResult, backupId);
    }
  }

  @Test
  public void shouldFailOnCreateOfExistingBackup() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateResponse>> supplierCreateResult = createSupplierCreate(
        asyncClient, creator -> creator
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withWaitForCompletion(true)
      );

      BackupTestSuite.testFailOnCreateOfExistingBackup(supplierCreateResult, backupId);
    }
  }

  @Test
  public void shouldFailOnCreateStatusOfNotExistingBackup() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
        asyncClient, createStatusGetter -> createStatusGetter
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(notExistingBackupId)
      );

      BackupTestSuite.testFailOnCreateStatusOfNotExistingBackup(supplierCreateStatusResult, notExistingBackupId);
    }
  }

  @Test
  public void shouldFailOnRestoreOfNotExistingBackup() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
        asyncClient, restorer -> restorer
          .withIncludeClassNames(BackupTestSuite.NOT_EXISTING_CLASS_NAME)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(notExistingBackupId)
      );

      BackupTestSuite.testFailOnRestoreOfNotExistingBackup(supplierRestoreResult, notExistingBackupId);
    }
  }

  @Test
  public void shouldFailOnRestoreBackupStatusOfNotStartedRestore() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateResponse>> supplierCreateResult = createSupplierCreate(
        asyncClient, creator -> creator
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withWaitForCompletion(true)
      );
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = createSupplierRestoreStatus(
        asyncClient, restoreStatusGetter -> restoreStatusGetter
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );

      BackupTestSuite.testFailOnRestoreBackupStatusOfNotStartedRestore(supplierCreateResult, supplierRestoreStatusResult, backupId);
    }
  }

  @Test
  public void shouldFailOnCreateBackupForBothIncludeAndExcludeClasses() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateResponse>> supplierCreateResult = createSupplierCreate(
        asyncClient, creator -> creator
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withExcludeClassNames(BackupTestSuite.CLASS_NAME_SOUP)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withWaitForCompletion(true)
      );

      BackupTestSuite.testFailOnCreateBackupForBothIncludeAndExcludeClasses(supplierCreateResult);
    }
  }

  @Test
  public void shouldFailOnRestoreBackupForBothIncludeAndExcludeClasses() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateResponse>> supplierCreateResult = createSupplierCreate(
        asyncClient, creator -> creator
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA, BackupTestSuite.CLASS_NAME_SOUP)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withWaitForCompletion(true)
      );
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
        asyncClient, restorer -> restorer
          .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
          .withExcludeClassNames(BackupTestSuite.CLASS_NAME_SOUP)
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );

      BackupTestSuite.testFailOnRestoreBackupForBothIncludeAndExcludeClasses(supplierCreateResult, supplierRestoreResult,
        createSupplierDeletePizza());
    }
  }

  @Test
  public void shouldCreateAndRestoreBackupWithWaitingWithConfig() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      // config with too high value
      Supplier<Result<BackupCreateResponse>> supplierCreateInvConfigResult = createSupplierCreate(
        asyncClient, creator -> {
          BackupCreator.BackupCreateConfig invCreateConfig = BackupCreator.BackupCreateConfig.builder()
            .cpuPercentage(801)
            .build();

          creator
            .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
            .withBackend(BackupTestSuite.BACKEND)
            .withBackupId(backupId)
            .withConfig(invCreateConfig)
            .withWaitForCompletion(true);
        }
      );
      // valid config
      Supplier<Result<BackupCreateResponse>> supplierCreateResult = createSupplierCreate(
        asyncClient, creator -> {
          BackupCreator.BackupCreateConfig createConfig = BackupCreator.BackupCreateConfig.builder()
            .cpuPercentage(80)
            .chunkSize(512)
            .compressionLevel(BackupCreator.BackupCompression.BEST_SPEED)
            .build();

          creator
            .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
            .withBackend(BackupTestSuite.BACKEND)
            .withBackupId(backupId)
            .withConfig(createConfig)
            .withWaitForCompletion(true);
        }
      );
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
        asyncClient, createStatusGetter -> createStatusGetter
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );
      // config with too high value
      Supplier<Result<BackupRestoreResponse>> supplierRestoreInvConfigResult = createSupplierRestore(
        asyncClient, restorer -> {
          BackupRestorer.BackupRestoreConfig invRestoreConfig = BackupRestorer.BackupRestoreConfig.builder()
            .cpuPercentage(90)
            .build();

          restorer
            .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
            .withBackend(BackupTestSuite.BACKEND)
            .withBackupId(backupId)
            .withConfig(invRestoreConfig)
            .withWaitForCompletion(true);
        }
      );
      // valid config
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
        asyncClient, restorer -> {
          BackupRestorer.BackupRestoreConfig restoreConfig = BackupRestorer.BackupRestoreConfig.builder()
            .cpuPercentage(70)
            .build();

          restorer
            .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
            .withBackend(BackupTestSuite.BACKEND)
            .withBackupId(backupId)
            .withConfig(restoreConfig)
            .withWaitForCompletion(true);
        }
      );
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = createSupplierRestoreStatus(
        asyncClient, restoreStatusGetter -> restoreStatusGetter
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );

      BackupTestSuite.testCreateAndRestoreBackupWithWaitingWithConfig(supplierCreateInvConfigResult, supplierCreateResult,
        supplierCreateStatusResult, supplierRestoreInvConfigResult, supplierRestoreResult, supplierRestoreStatusResult,
        createSupplierDeletePizza(), createSupplierGQLOfClass(), backupId);
    }
  }

  @Test
  public void shouldCancelBackup() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateResponse>> supplierCreateResult = createSupplierCreate(
        asyncClient, creator -> creator
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
          .withWaitForCompletion(false) // this will allow us to "intercept" the backup in progress
      );
      Supplier<Result<Void>> supplierCancelResult = createSupplierCanceler(
        asyncClient, canceler -> canceler
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
        asyncClient, createStatusGetter -> createStatusGetter
          .withBackend(BackupTestSuite.BACKEND)
          .withBackupId(backupId)
      );

      BackupTestSuite.testCancelBackup(supplierCreateResult, supplierCancelResult, supplierCreateStatusResult);
    }
  }


  @NotNull
  private Supplier<Result<Boolean>> createSupplierDeletePizza() {
    return () -> client.schema().classDeleter()
      .withClassName(BackupTestSuite.CLASS_NAME_PIZZA)
      .run();
  }

  @NotNull
  private Function<String, Result<GraphQLResponse>> createSupplierGQLOfClass() {
    return (String className) -> client.graphQL().get()
      .withClassName(className)
      .withFields(Field.builder().name("name").build())
      .run();
  }

  private Supplier<Result<BackupCreateResponse>> createSupplierCreate(WeaviateAsyncClient asyncClient,
                                                                      Consumer<BackupCreator> configure) {
    return () -> {
      try {
        BackupCreator creator = asyncClient.backup().creator();
        configure.accept(creator);
        return creator.run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };
  }

  private Supplier<Result<BackupCreateResponse[]>> createSupplierGet(WeaviateAsyncClient asyncClient,
                                                                     Consumer<BackupGetter> configure) {
    return () -> {
      try {
        BackupGetter getter = asyncClient.backup().getter();
        configure.accept(getter);
        return getter.run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };                              
  }

  private Supplier<Result<BackupCreateStatusResponse>> createSupplierCreateStatus(WeaviateAsyncClient asyncClient,
                                                                                  Consumer<BackupCreateStatusGetter> configure) {
    return () -> {
      try {
        BackupCreateStatusGetter getter = asyncClient.backup().createStatusGetter();
        configure.accept(getter);
        return getter.run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };
  }

  private Supplier<Result<BackupRestoreResponse>> createSupplierRestore(WeaviateAsyncClient asyncClient,
                                                                        Consumer<BackupRestorer> configure) {
    return () -> {
      try {
        BackupRestorer restorer = asyncClient.backup().restorer();
        configure.accept(restorer);
        return restorer.run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };
  }

  private Supplier<Result<BackupRestoreStatusResponse>> createSupplierRestoreStatus(WeaviateAsyncClient asyncClient,
                                                                                    Consumer<BackupRestoreStatusGetter> configure) {
    return () -> {
      try {
        BackupRestoreStatusGetter getter = asyncClient.backup().restoreStatusGetter();
        configure.accept(getter);
        return getter.run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };
  }

  private Supplier<Result<Void>> createSupplierCanceler(WeaviateAsyncClient asyncClient,
                                                        Consumer<BackupCanceler> configure) {
    return () -> {
      try {
        BackupCanceler canceler = asyncClient.backup().canceler();
        configure.accept(canceler);
        return canceler.run().get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    };
  }
}
