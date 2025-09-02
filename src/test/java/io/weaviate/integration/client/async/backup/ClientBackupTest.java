package io.weaviate.integration.client.async.backup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.aliases.model.Alias;
import io.weaviate.client.v1.async.WeaviateAsyncClient;
import io.weaviate.client.v1.async.backup.api.BackupCanceler;
import io.weaviate.client.v1.async.backup.api.BackupCreateStatusGetter;
import io.weaviate.client.v1.async.backup.api.BackupCreator;
import io.weaviate.client.v1.async.backup.api.BackupGetter;
import io.weaviate.client.v1.async.backup.api.BackupRestoreStatusGetter;
import io.weaviate.client.v1.async.backup.api.BackupRestorer;
import io.weaviate.client.v1.async.backup.api.BackupRestorer.BackupRestoreConfig;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.backup.model.BackupCreateResponse;
import io.weaviate.client.v1.backup.model.BackupCreateStatusResponse;
import io.weaviate.client.v1.backup.model.BackupRestoreResponse;
import io.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;
import io.weaviate.client.v1.backup.model.RbacRestoreOption;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.rbac.model.ClusterPermission;
import io.weaviate.client.v1.rbac.model.Permission;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.integration.client.WeaviateDockerComposeBackup;
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
  public static WeaviateDockerComposeBackup compose = new WeaviateDockerComposeBackup();

  @Before
  public void before() throws AuthException {
    Config config = new Config("http", compose.getHttpHostAddress());
    client = WeaviateAuthClient.apiKey(config, WeaviateDockerComposeBackup.ADMIN_KEY);
    testGenerics.createTestSchemaAndData(client);

    backupId = String.format("backup-%s-%s", currentTest.getMethodName().toLowerCase(),
        rand.nextInt(Integer.MAX_VALUE));
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
              .withWaitForCompletion(true));
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
          asyncClient, createStatusGetter -> createStatusGetter
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId));
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
          asyncClient, restorer -> restorer
              .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId)
              .withWaitForCompletion(true));
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = createSupplierRestoreStatus(
          asyncClient, restoreStatusGetter -> restoreStatusGetter
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId));

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
              .withBackupId(backupId));
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
          asyncClient, createStatusGetter -> createStatusGetter
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId));
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
          asyncClient, restorer -> restorer
              .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId));
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = createSupplierRestoreStatus(
          asyncClient, restoreStatusGetter -> restoreStatusGetter
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId));

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
              .withConfig(BackupCreator.BackupCreateConfig.builder().bucket(bucket).path(path).build()));
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
          asyncClient, createStatusGetter -> createStatusGetter
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId)
              .withBucket(bucket)
              .withPath(path));
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
          asyncClient, restorer -> restorer
              .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId)
              .withConfig(BackupRestorer.BackupRestoreConfig.builder().bucket(bucket).path(path).build()));
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = createSupplierRestoreStatus(
          asyncClient, restoreStatusGetter -> restoreStatusGetter
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId)
              .withBucket(bucket)
              .withPath(path));

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
              .withWaitForCompletion(true));
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
          asyncClient, createStatusGetter -> createStatusGetter
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId));
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
          asyncClient, restorer -> restorer
              .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId)
              .withWaitForCompletion(true));
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = createSupplierRestoreStatus(
          asyncClient, restoreStatusGetter -> restoreStatusGetter
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId));

      BackupTestSuite.testCreateAndRestore1Of2Classes(supplierCreateResult, supplierCreateStatusResult,
          supplierRestoreResult, supplierRestoreStatusResult,
          createSupplierDeletePizza(), createSupplierGQLOfClass(), backupId);
    }
  }

  @Test
  public void shouldListCreatedBackups() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      List<Supplier<Result<BackupCreateResponse>>> createSuppliers = new ArrayList<Supplier<Result<BackupCreateResponse>>>() {
        {
          this.add(createSupplierCreate(
              asyncClient, creator -> creator
                  .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
                  .withBackend(BackupTestSuite.BACKEND)
                  .withBackupId(backupId + "-1")
                  .withWaitForCompletion(true)));
          this.add(createSupplierCreate(
              asyncClient, creator -> creator
                  .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
                  .withBackend(BackupTestSuite.BACKEND)
                  .withBackupId(backupId + "-2")
                  .withWaitForCompletion(true)));
        }
      };

      Supplier<Result<BackupCreateResponse[]>> supplierGetResult = createSupplierGet(
          asyncClient, creator -> creator
              .withBackend(BackupTestSuite.BACKEND));

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
              .withBackupId(backupId));

      BackupTestSuite.testFailOnCreateBackupOnNotExistingBackend(supplierCreateResult);
    }
  }

  @Test
  public void shouldFailOnCreateBackupStatusOnNotExistingBackend() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
          asyncClient, createStatusGetter -> createStatusGetter
              .withBackend(BackupTestSuite.NOT_EXISTING_BACKEND)
              .withBackupId(backupId));

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
              .withBackupId(backupId));

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
              .withBackupId(backupId));

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
              .withWaitForCompletion(true));
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
          asyncClient, restorer -> restorer
              .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId)
              .withWaitForCompletion(true));

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
              .withWaitForCompletion(true));

      BackupTestSuite.testFailOnCreateOfExistingBackup(supplierCreateResult, backupId);
    }
  }

  @Test
  public void shouldFailOnCreateStatusOfNotExistingBackup() {
    try (WeaviateAsyncClient asyncClient = client.async()) {
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
          asyncClient, createStatusGetter -> createStatusGetter
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(notExistingBackupId));

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
              .withBackupId(notExistingBackupId));

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
              .withWaitForCompletion(true));
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = createSupplierRestoreStatus(
          asyncClient, restoreStatusGetter -> restoreStatusGetter
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId));

      BackupTestSuite.testFailOnRestoreBackupStatusOfNotStartedRestore(supplierCreateResult,
          supplierRestoreStatusResult, backupId);
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
              .withWaitForCompletion(true));

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
              .withWaitForCompletion(true));
      Supplier<Result<BackupRestoreResponse>> supplierRestoreResult = createSupplierRestore(
          asyncClient, restorer -> restorer
              .withIncludeClassNames(BackupTestSuite.CLASS_NAME_PIZZA)
              .withExcludeClassNames(BackupTestSuite.CLASS_NAME_SOUP)
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId));

      BackupTestSuite.testFailOnRestoreBackupForBothIncludeAndExcludeClasses(supplierCreateResult,
          supplierRestoreResult,
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
          });
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
          });
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
          asyncClient, createStatusGetter -> createStatusGetter
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId));
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
          });
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
          });
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatusResult = createSupplierRestoreStatus(
          asyncClient, restoreStatusGetter -> restoreStatusGetter
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId));

      BackupTestSuite.testCreateAndRestoreBackupWithWaitingWithConfig(supplierCreateInvConfigResult,
          supplierCreateResult,
          supplierCreateStatusResult, supplierRestoreInvConfigResult, supplierRestoreResult,
          supplierRestoreStatusResult,
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
              .withBackupId(backupId));
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatusResult = createSupplierCreateStatus(
          asyncClient, createStatusGetter -> createStatusGetter
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId));

      BackupTestSuite.testCancelBackup(supplierCreateResult, supplierCancelResult, supplierCreateStatusResult);
    }
  }

  @Test
  public void shouldRestoreWithRbacOptions() {
    final String className = "RolesUsers";
    final String roleName = "restoreRole";
    final String userName = "restoreUser";

    try (final WeaviateAsyncClient async = client.async()) {

      BackupTestSuite.testBackupRestoreWithRbacOptions(backupId,
          // Arrange: create collection, create role, create user;
          runnable(() -> {
            async.schema().classDeleter().withClassName(className).run().get();
            async.schema().classCreator().withClass(WeaviateClass.builder().className(className).build()).run().get();

            async.roles().deleter().withName(roleName).run().get();
            Result<?> createRole = async.roles().creator().withName(roleName)
                .withPermissions(Permission.cluster(ClusterPermission.Action.READ)).run().get();
            Assertions.assertThat(createRole.getError()).as("create role").isNull();

            async.users().db().deleter().withUserId(userName).run().get();
            Result<?> createUser = async.users().db().creator().withUserId(userName).run().get();
            Assertions.assertThat(createUser.getError()).as("create user").isNull();

            return null; // satisfy Callable
          }),
          runnable(() -> {
            async.schema().classDeleter().withClassName(className).run().get();
            async.roles().deleter().withName(roleName).run().get();
            async.users().db().deleter().withUserId(userName).run().get();

            return null; // satisfy Callable
          }),
          // Create backup
          supplier(() -> async.backup().creator()
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId)
              .withIncludeClassNames("RolesUsers")
              .withWaitForCompletion(true)
              .run().get()),
          // Restore from backup
          supplier(() -> async.backup().restorer()
              .withBackend(BackupTestSuite.BACKEND)
              .withBackupId(backupId)
              .withIncludeClassNames("RolesUsers")
              .withWaitForCompletion(true)
              .withConfig(BackupRestoreConfig.builder()
                  .usersRestore(RbacRestoreOption.ALL)
                  .rolesRestore(RbacRestoreOption.ALL)
                  .build())
              .run().get()),
          supplier(() -> async.users().db().getUser().withUserId(userName).run().get()),
          supplier(() -> async.roles().getter().withName(roleName).run().get()));
    }
  }

  @Test
  public void testOverwriteAlias_true() throws InterruptedException, ExecutionException, Exception {
    String originalClass = "CollectionOverwriteAlias";
    String alias = originalClass + "Alias";
    String differentClass = "Different" + originalClass;

    try (final WeaviateAsyncClient async = client.async()) {
      Runnable arrange = runnable(() -> {
        Result<?> res;

        res = async.schema().classCreator()
            .withClass(WeaviateClass.builder().className(originalClass).build())
            .run().get();
        Assertions.assertThat(res.getError()).isNull();
        res = async.alias().creator().withClassName(originalClass).withAlias(alias).run().get();
        Assertions.assertThat(res.getError()).isNull();

        res = async.backup().creator()
            .withBackupId(backupId)
            .withBackend(BackupTestSuite.BACKEND)
            .withIncludeClassNames(originalClass)
            .withWaitForCompletion(true)
            .run().get();
        Assertions.assertThat(res.getError()).isNull();

        res = async.schema().classDeleter().withClassName(originalClass).run().get();
        Assertions.assertThat(res.getError()).isNull();
        res = async.schema().classCreator()
            .withClass(WeaviateClass.builder().className(differentClass).build())
            .run().get();
        Assertions.assertThat(res.getError()).isNull();
        res = async.alias().updater().withAlias(alias).withNewClassName(differentClass).run().get();
        Assertions.assertThat(res.getError()).isNull();

        return null; // satisfy Callable
      });

      Callable<Result<?>> act = () -> async.backup().restorer()
          .withBackupId(backupId)
          .withBackend(BackupTestSuite.BACKEND)
          .withIncludeClassNames(originalClass)
          .withWaitForCompletion(true)
          .withOverwriteAlias(true)
          .run().get();

      Supplier<Alias> getAlias = supplier(() -> async.alias().getter().withAlias(alias).run().get().getResult());

      BackupTestSuite.testOverwriteAlias_true(arrange, act, getAlias, originalClass);
    }
  }

  @FunctionalInterface
  interface ThrowingSupplier<T> {
    T get() throws Exception;
  }

  /** Convert throwing Callable into a Runnable which does not throw. */
  private static Runnable runnable(Callable<Exception> c) {
    return () -> {
      try {
        c.call();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  /** Convert throwing Supplier into one that does not throw. */
  private static <T> Supplier<T> supplier(ThrowingSupplier<T> s) {
    return () -> {
      try {
        return s.get();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
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
