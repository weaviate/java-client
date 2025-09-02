package io.weaviate.integration.tests.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;
import static org.assertj.core.api.InstanceOfAssertFactories.CHAR_SEQUENCE;
import static org.junit.Assume.assumeTrue;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.http.HttpStatus;

import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.aliases.model.Alias;
import io.weaviate.client.v1.backup.model.Backend;
import io.weaviate.client.v1.backup.model.BackupCreateResponse;
import io.weaviate.client.v1.backup.model.BackupCreateStatusResponse;
import io.weaviate.client.v1.backup.model.BackupRestoreResponse;
import io.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;
import io.weaviate.client.v1.backup.model.CreateStatus;
import io.weaviate.client.v1.backup.model.RestoreStatus;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.rbac.model.Role;
import io.weaviate.client.v1.users.model.UserDb;

public class BackupTestSuite {

  public static final String DOCKER_COMPOSE_BACKUPS_DIR = "/tmp/backups";
  public static final String CLASS_NAME_PIZZA = "Pizza";
  public static final String CLASS_NAME_SOUP = "Soup";
  public static final String NOT_EXISTING_CLASS_NAME = "not-existing-class";
  public static final String BACKEND = Backend.FILESYSTEM;
  public static final String NOT_EXISTING_BACKEND = "not-existing-backend";

  public static void testCreateAndRestoreBackupWithWaiting(Supplier<Result<BackupCreateResponse>> supplierCreate,
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatus,
      Supplier<Result<BackupRestoreResponse>> supplierRestore,
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatus,
      Supplier<Result<Boolean>> supplierDeleteClass,
      Function<String, Result<GraphQLResponse>> supplierGQLOfClass,
      String backupId) {
    assertThatAllPizzasExist(supplierGQLOfClass);

    // Create backup
    Result<BackupCreateResponse> createResult = supplierCreate.get();

    assertThat(createResult.getError()).as("create backup").isNull();
    assertThat(createResult.getResult()).isNotNull()
        .returns(backupId, BackupCreateResponse::getId)
        .returns(new String[] { CLASS_NAME_PIZZA }, BackupCreateResponse::getClassNames)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateResponse::getPath)
        .returns(BACKEND, BackupCreateResponse::getBackend)
        .returns(CreateStatus.SUCCESS, BackupCreateResponse::getStatus)
        .returns(null, BackupCreateResponse::getError);

    assertThatAllPizzasExist(supplierGQLOfClass);

    // Check backup status
    Result<BackupCreateStatusResponse> createStatusResult = supplierCreateStatus.get();

    assertThat(createStatusResult.getError()).as("check backup creation status").isNull();
    assertThat(createStatusResult.getResult()).isNotNull()
        .returns(backupId, BackupCreateStatusResponse::getId)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateStatusResponse::getPath)
        .returns(BACKEND, BackupCreateStatusResponse::getBackend)
        .returns(CreateStatus.SUCCESS, BackupCreateStatusResponse::getStatus)
        .returns(null, BackupCreateStatusResponse::getError);

    // Remove existing class
    Result<Boolean> delete = supplierDeleteClass.get();

    assertThat(delete.getError()).as("drop Pizza collection").isNull();
    assertThat(delete.getResult()).isTrue();

    // Restore backup
    Result<BackupRestoreResponse> restoreResult = supplierRestore.get();

    assertThat(restoreResult.getError()).as("restore from backup").isNull();
    assertThat(restoreResult.getResult()).isNotNull()
        .returns(backupId, BackupRestoreResponse::getId)
        .returns(new String[] { CLASS_NAME_PIZZA }, BackupRestoreResponse::getClassNames)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreResponse::getPath)
        .returns(BACKEND, BackupRestoreResponse::getBackend)
        .returns(RestoreStatus.SUCCESS, BackupRestoreResponse::getStatus)
        .returns(null, BackupRestoreResponse::getError);

    assertThatAllPizzasExist(supplierGQLOfClass);

    // Check restore backup
    Result<BackupRestoreStatusResponse> restoreStatusResult = supplierRestoreStatus.get();

    assertThat(restoreStatusResult.getError()).as("get restore status").isNull();
    assertThat(restoreStatusResult.getResult()).isNotNull()
        .returns(backupId, BackupRestoreStatusResponse::getId)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreStatusResponse::getPath)
        .returns(BACKEND, BackupRestoreStatusResponse::getBackend)
        .returns(RestoreStatus.SUCCESS, BackupRestoreStatusResponse::getStatus)
        .returns(null, BackupRestoreStatusResponse::getError);
  }

  public static void testCreateWithDynamicLocation(Supplier<Result<BackupCreateResponse>> supplierCreate,
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatus,
      Supplier<Result<BackupRestoreResponse>> supplierRestore,
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatus,
      Supplier<Result<Boolean>> supplierDeleteClass,
      Function<String, Result<GraphQLResponse>> supplierGQLOfClass,
      String backupId, String bucket, String path) throws InterruptedException {
    assertThatAllPizzasExist(supplierGQLOfClass);
    String wantFullPath = Paths.get(path, backupId).toString();

    Result<BackupCreateResponse> createResult = supplierCreate.get();
    assertThat(createResult.getError()).as("create backup").isNull();
    assertThat(createResult.getResult()).isNotNull()
        .returns(backupId, BackupCreateResponse::getId)
        .returns(wantFullPath, BackupCreateResponse::getPath).as("path in BackupCreateResponse");

    // Wait until created
    Result<BackupCreateStatusResponse> createStatusResult;
    while (true) {
      createStatusResult = supplierCreateStatus.get();

      assertThat(createStatusResult.getError()).as("check backup creation status").isNull();
      assertThat(createStatusResult.getResult()).isNotNull()
          .returns(backupId, BackupCreateStatusResponse::getId)
          .returns(wantFullPath, BackupCreateStatusResponse::getPath)
          .extracting(BackupCreateStatusResponse::getStatus)
          .isIn(CreateStatus.STARTED, CreateStatus.TRANSFERRING, CreateStatus.TRANSFERRED, CreateStatus.SUCCESS);

      if (CreateStatus.SUCCESS.equals(createStatusResult.getResult().getStatus())) {
        break;
      }
      Thread.sleep(100);
    }

    // Delete all data to then restore it from backup.
    Result<Boolean> delete = supplierDeleteClass.get();
    assertThat(delete.getError()).as("drop Pizza collection").isNull();
    assertThat(delete.getResult()).isTrue();

    Result<BackupRestoreResponse> restoreResult = supplierRestore.get();
    assertThat(restoreResult.getError()).as("restore from backup").isNull();
    assertThat(restoreResult.getResult()).isNotNull()
        .returns(backupId, BackupRestoreResponse::getId)
        .returns(wantFullPath, BackupRestoreResponse::getPath);

    // Wait until restored
    Result<BackupRestoreStatusResponse> restoreStatusResult;
    while (true) {
      restoreStatusResult = supplierRestoreStatus.get();

      assertThat(restoreStatusResult.getError()).as("get restore status").isNull();
      assertThat(restoreStatusResult.getResult()).isNotNull()
          .returns(backupId, BackupRestoreStatusResponse::getId)
          .returns(wantFullPath, BackupRestoreStatusResponse::getPath)
          .extracting(BackupRestoreStatusResponse::getStatus)
          .isIn(RestoreStatus.STARTED, RestoreStatus.TRANSFERRING, RestoreStatus.TRANSFERRED, RestoreStatus.SUCCESS);

      if (RestoreStatus.SUCCESS.equals(restoreStatusResult.getResult().getStatus())) {
        break;
      }
      Thread.sleep(100);
    }

    assertThatAllPizzasExist(supplierGQLOfClass);
  }

  public static void testCreateAndRestoreBackupWithoutWaiting(Supplier<Result<BackupCreateResponse>> supplierCreate,
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatus,
      Supplier<Result<BackupRestoreResponse>> supplierRestore,
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatus,
      Supplier<Result<Boolean>> supplierDeleteClass,
      Function<String, Result<GraphQLResponse>> supplierGQLOfClass,
      String backupId) throws InterruptedException {
    assertThatAllPizzasExist(supplierGQLOfClass);

    // Start creating backup
    Result<BackupCreateResponse> createResult = supplierCreate.get();

    assertThat(createResult.getError()).as("create backup").isNull();
    assertThat(createResult.getResult()).isNotNull()
        .returns(backupId, BackupCreateResponse::getId)
        .returns(new String[] { CLASS_NAME_PIZZA }, BackupCreateResponse::getClassNames)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateResponse::getPath)
        .returns(BACKEND, BackupCreateResponse::getBackend)
        .returns(CreateStatus.STARTED, BackupCreateResponse::getStatus)
        .returns(null, BackupCreateResponse::getError);

    // Wait until created
    Result<BackupCreateStatusResponse> createStatusResult;
    while (true) {
      createStatusResult = supplierCreateStatus.get();

      assertThat(createStatusResult.getError()).as("check backup creation status").isNull();
      assertThat(createStatusResult.getResult()).isNotNull()
          .returns(backupId, BackupCreateStatusResponse::getId)
          .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateStatusResponse::getPath)
          .returns(BACKEND, BackupCreateStatusResponse::getBackend)
          .returns(null, BackupCreateStatusResponse::getError)
          .extracting(BackupCreateStatusResponse::getStatus).isIn(CreateStatus.STARTED, CreateStatus.TRANSFERRING,
              CreateStatus.TRANSFERRED, CreateStatus.SUCCESS);

      if (CreateStatus.SUCCESS.equals(createStatusResult.getResult().getStatus())) {
        break;
      }
      Thread.sleep(100);
    }

    assertThatAllPizzasExist(supplierGQLOfClass);

    // Remove existing class
    Result<Boolean> delete = supplierDeleteClass.get();

    assertThat(delete.getError()).as("drop Pizza collection").isNull();
    assertThat(delete.getResult()).isTrue();

    // Start restoring backup
    Result<BackupRestoreResponse> restoreResult = supplierRestore.get();

    assertThat(restoreResult.getError()).as("restore from backup").isNull();
    assertThat(restoreResult.getResult()).isNotNull()
        .returns(backupId, BackupRestoreResponse::getId)
        .returns(new String[] { CLASS_NAME_PIZZA }, BackupRestoreResponse::getClassNames)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreResponse::getPath)
        .returns(BACKEND, BackupRestoreResponse::getBackend)
        .returns(RestoreStatus.STARTED, BackupRestoreResponse::getStatus)
        .returns(null, BackupRestoreResponse::getError);

    // Wait until restored
    Result<BackupRestoreStatusResponse> restoreStatusResult;
    while (true) {
      restoreStatusResult = supplierRestoreStatus.get();

      assertThat(restoreStatusResult.getError()).as("get restore status").isNull();
      assertThat(restoreStatusResult.getResult()).isNotNull()
          .returns(backupId, BackupRestoreStatusResponse::getId)
          .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreStatusResponse::getPath)
          .returns(BACKEND, BackupRestoreStatusResponse::getBackend)
          .returns(null, BackupRestoreStatusResponse::getError)
          .extracting(BackupRestoreStatusResponse::getStatus).isIn(RestoreStatus.STARTED, RestoreStatus.TRANSFERRING,
              RestoreStatus.TRANSFERRED, RestoreStatus.SUCCESS);

      if (RestoreStatus.SUCCESS.equals(restoreStatusResult.getResult().getStatus())) {
        break;
      }
      Thread.sleep(100);
    }

    assertThatAllPizzasExist(supplierGQLOfClass);
  }

  public static void testCreateAndRestore1Of2Classes(Supplier<Result<BackupCreateResponse>> supplierCreate,
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatus,
      Supplier<Result<BackupRestoreResponse>> supplierRestore,
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatus,
      Supplier<Result<Boolean>> supplierDeleteClass,
      Function<String, Result<GraphQLResponse>> supplierGQLOfClass,
      String backupId) {
    assertThatAllPizzasExist(supplierGQLOfClass);
    assertThatAllSoupsExist(supplierGQLOfClass);

    // Create backup for all existing classes (2)
    Result<BackupCreateResponse> createResult = supplierCreate.get();

    assertThat(createResult.hasErrors()).isFalse();
    assertThat(createResult.getResult()).isNotNull()
        .returns(backupId, BackupCreateResponse::getId)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateResponse::getPath)
        .returns(BACKEND, BackupCreateResponse::getBackend)
        .returns(CreateStatus.SUCCESS, BackupCreateResponse::getStatus)
        .returns(null, BackupCreateResponse::getError)
        .extracting(BackupCreateResponse::getClassNames).asInstanceOf(ARRAY)
        .containsExactlyInAnyOrder(CLASS_NAME_PIZZA, CLASS_NAME_SOUP);

    assertThatAllPizzasExist(supplierGQLOfClass);
    assertThatAllSoupsExist(supplierGQLOfClass);

    // Check backup status
    Result<BackupCreateStatusResponse> createStatusResult = supplierCreateStatus.get();

    assertThat(createStatusResult.hasErrors()).isFalse();
    assertThat(createStatusResult.getResult()).isNotNull()
        .returns(backupId, BackupCreateStatusResponse::getId)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateStatusResponse::getPath)
        .returns(BACKEND, BackupCreateStatusResponse::getBackend)
        .returns(CreateStatus.SUCCESS, BackupCreateStatusResponse::getStatus)
        .returns(null, BackupCreateStatusResponse::getError);

    // Remove existing class
    Result<Boolean> delete = supplierDeleteClass.get();

    assertThat(delete.hasErrors()).isFalse();
    assertThat(delete.getResult()).isTrue();

    // Restore backup
    Result<BackupRestoreResponse> restoreResult = supplierRestore.get();

    assertThat(restoreResult.hasErrors()).isFalse();
    assertThat(restoreResult.getResult()).isNotNull()
        .returns(backupId, BackupRestoreResponse::getId)
        .returns(new String[] { CLASS_NAME_PIZZA }, BackupRestoreResponse::getClassNames)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreResponse::getPath)
        .returns(BACKEND, BackupRestoreResponse::getBackend)
        .returns(RestoreStatus.SUCCESS, BackupRestoreResponse::getStatus)
        .returns(null, BackupRestoreResponse::getError);

    assertThatAllPizzasExist(supplierGQLOfClass);
    assertThatAllSoupsExist(supplierGQLOfClass);

    // Check restore backup
    Result<BackupRestoreStatusResponse> restoreStatusResult = supplierRestoreStatus.get();

    assertThat(restoreStatusResult.hasErrors()).isFalse();
    assertThat(restoreStatusResult.getResult()).isNotNull()
        .returns(backupId, BackupRestoreStatusResponse::getId)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreStatusResponse::getPath)
        .returns(BACKEND, BackupRestoreStatusResponse::getBackend)
        .returns(RestoreStatus.SUCCESS, BackupRestoreStatusResponse::getStatus)
        .returns(null, BackupRestoreStatusResponse::getError);
  }

  public static void testListExistingBackups(List<Supplier<Result<BackupCreateResponse>>> createSuppliers,
      Supplier<Result<BackupCreateResponse[]>> supplierGet) {
    // Create backups
    createSuppliers.forEach(Supplier::get);

    // List backups
    Result<BackupCreateResponse[]> listResult = supplierGet.get();
    skipIfNotImplemented(listResult);

    assertThat(listResult.getError()).isNull();
    assertThat(listResult.getResult()).isNotNull()
        .hasSizeGreaterThan(2);
  }

  public static void testFailOnCreateBackupOnNotExistingBackend(Supplier<Result<BackupCreateResponse>> supplierCreate) {
    Result<BackupCreateResponse> createResult = supplierCreate.get();

    assertThat(createResult.hasErrors()).isTrue();
    assertThat(createResult.getError()).isNotNull()
        .returns(HttpStatus.SC_UNPROCESSABLE_ENTITY, WeaviateError::getStatusCode)
        .extracting(WeaviateError::getMessages).asList()
        .hasSizeGreaterThan(0)
        .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
        .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_BACKEND);
  }

  public static void testFailOnCreateBackupStatusOnNotExistingBackend(
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatus) {
    Result<BackupCreateStatusResponse> createStatusResult = supplierCreateStatus.get();

    assertThat(createStatusResult.hasErrors()).isTrue();
    assertThat(createStatusResult.getError()).isNotNull()
        .returns(HttpStatus.SC_UNPROCESSABLE_ENTITY, WeaviateError::getStatusCode)
        .extracting(WeaviateError::getMessages).asList()
        .hasSizeGreaterThan(0)
        .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
        .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_BACKEND);
  }

  public static void testFailOnRestoreBackupFromNotExistingBackend(
      Supplier<Result<BackupRestoreResponse>> supplierRestore) {
    Result<BackupRestoreResponse> restoreResult = supplierRestore.get();

    assertThat(restoreResult.hasErrors()).isTrue();
    assertThat(restoreResult.getError()).isNotNull()
        .returns(HttpStatus.SC_UNPROCESSABLE_ENTITY, WeaviateError::getStatusCode)
        .extracting(WeaviateError::getMessages).asList()
        .hasSizeGreaterThan(0)
        .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
        .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_BACKEND);
  }

  public static void testFailOnCreateBackupForNotExistingClass(Supplier<Result<BackupCreateResponse>> supplierCreate) {
    Result<BackupCreateResponse> createResult = supplierCreate.get();

    assertThat(createResult.hasErrors()).isTrue();
    assertThat(createResult.getError()).isNotNull()
        .returns(HttpStatus.SC_UNPROCESSABLE_ENTITY, WeaviateError::getStatusCode)
        .extracting(WeaviateError::getMessages).asList()
        .hasSizeGreaterThan(0)
        .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
        .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_CLASS_NAME);
  }

  public static void testFailOnRestoreBackupForExistingClass(Supplier<Result<BackupCreateResponse>> supplierCreate,
      Supplier<Result<BackupRestoreResponse>> supplierRestore,
      String backupId) {
    Result<BackupCreateResponse> createResult = supplierCreate.get();

    assertThat(createResult.hasErrors()).isFalse();

    Result<BackupRestoreResponse> restoreResult = supplierRestore.get();

    assertThat(restoreResult.hasErrors()).isFalse();
    assertThat(restoreResult.getResult()).isNotNull()
        .returns(backupId, BackupRestoreResponse::getId)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreResponse::getPath)
        .returns(BACKEND, BackupRestoreResponse::getBackend)
        .returns(RestoreStatus.FAILED, BackupRestoreResponse::getStatus)
        .returns("could not restore classes: [\"Pizza\": class name Pizza already exists]",
            BackupRestoreResponse::getError);
  }

  public static void testFailOnCreateOfExistingBackup(Supplier<Result<BackupCreateResponse>> supplierCreate,
      String backupId) {
    Result<BackupCreateResponse> createResult = supplierCreate.get();

    assertThat(createResult.hasErrors()).isFalse();

    Result<BackupCreateResponse> createResultAgain = supplierCreate.get();

    assertThat(createResultAgain.hasErrors()).isTrue();
    assertThat(createResultAgain.getError()).isNotNull()
        .returns(HttpStatus.SC_UNPROCESSABLE_ENTITY, WeaviateError::getStatusCode)
        .extracting(WeaviateError::getMessages).asList()
        .hasSizeGreaterThan(0)
        .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
        .first().asInstanceOf(CHAR_SEQUENCE).contains(backupId);
  }

  public static void testFailOnCreateStatusOfNotExistingBackup(
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatus,
      String backupId) {
    Result<BackupCreateStatusResponse> createStatusResult = supplierCreateStatus.get();

    assertThat(createStatusResult.hasErrors()).isTrue();
    assertThat(createStatusResult.getError()).isNotNull()
        .returns(HttpStatus.SC_NOT_FOUND, WeaviateError::getStatusCode)
        .extracting(WeaviateError::getMessages).asList()
        .hasSizeGreaterThan(0)
        .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
        .first().asInstanceOf(CHAR_SEQUENCE).contains(backupId);
  }

  public static void testFailOnRestoreOfNotExistingBackup(Supplier<Result<BackupRestoreResponse>> supplierRestore,
      String backupId) {
    Result<BackupRestoreResponse> restoreResult = supplierRestore.get();

    assertThat(restoreResult.hasErrors()).isTrue();
    assertThat(restoreResult.getError()).isNotNull()
        .returns(HttpStatus.SC_NOT_FOUND, WeaviateError::getStatusCode)
        .extracting(WeaviateError::getMessages).asList()
        .hasSizeGreaterThan(0)
        .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
        .first().asInstanceOf(CHAR_SEQUENCE).contains(backupId);
  }

  public static void testFailOnRestoreBackupStatusOfNotStartedRestore(
      Supplier<Result<BackupCreateResponse>> supplierCreate,
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatus,
      String backupId) {
    Result<BackupCreateResponse> createResult = supplierCreate.get();

    assertThat(createResult.hasErrors()).isFalse();

    Result<BackupRestoreStatusResponse> restoreStatusResult = supplierRestoreStatus.get();

    assertThat(restoreStatusResult.hasErrors()).isTrue();
    assertThat(restoreStatusResult.getError()).isNotNull()
        .returns(HttpStatus.SC_NOT_FOUND, WeaviateError::getStatusCode)
        .extracting(WeaviateError::getMessages).asList()
        .hasSizeGreaterThan(0)
        .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
        .first().asInstanceOf(CHAR_SEQUENCE).contains(backupId);
  }

  public static void testFailOnCreateBackupForBothIncludeAndExcludeClasses(
      Supplier<Result<BackupCreateResponse>> supplierCreate) {
    Result<BackupCreateResponse> createResult = supplierCreate.get();

    assertThat(createResult.hasErrors()).isTrue();
    assertThat(createResult.getError()).isNotNull()
        .returns(HttpStatus.SC_UNPROCESSABLE_ENTITY, WeaviateError::getStatusCode)
        .extracting(WeaviateError::getMessages).asList()
        .hasSizeGreaterThan(0)
        .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
        .first().asInstanceOf(CHAR_SEQUENCE).contains("include").contains("exclude");
  }

  public static void testFailOnRestoreBackupForBothIncludeAndExcludeClasses(
      Supplier<Result<BackupCreateResponse>> supplierCreate,
      Supplier<Result<BackupRestoreResponse>> supplierRestore,
      Supplier<Result<Boolean>> supplierDeleteClass) {
    // Create backup
    Result<BackupCreateResponse> createResult = supplierCreate.get();

    assertThat(createResult.hasErrors()).isFalse();

    // Remove existing class
    Result<Boolean> delete = supplierDeleteClass.get();

    assertThat(delete.hasErrors()).isFalse();

    // Restore
    Result<BackupRestoreResponse> restoreResult = supplierRestore.get();

    assertThat(restoreResult.hasErrors()).isTrue();
    assertThat(restoreResult.getError()).isNotNull()
        .returns(HttpStatus.SC_UNPROCESSABLE_ENTITY, WeaviateError::getStatusCode)
        .extracting(WeaviateError::getMessages).asList()
        .hasSizeGreaterThan(0)
        .extracting(msg -> ((WeaviateErrorMessage) msg).getMessage())
        .first().asInstanceOf(CHAR_SEQUENCE).contains("include").contains("exclude");
  }

  public static void testCreateAndRestoreBackupWithWaitingWithConfig(
      Supplier<Result<BackupCreateResponse>> supplierCreateInvConfig,
      Supplier<Result<BackupCreateResponse>> supplierCreate,
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatus,
      Supplier<Result<BackupRestoreResponse>> supplierRestoreInvConfig,
      Supplier<Result<BackupRestoreResponse>> supplierRestore,
      Supplier<Result<BackupRestoreStatusResponse>> supplierRestoreStatus,
      Supplier<Result<Boolean>> supplierDeleteClass,
      Function<String, Result<GraphQLResponse>> supplierGQLOfClass,
      String backupId) {
    assertThatAllPizzasExist(supplierGQLOfClass);

    // Try to create with too high value
    Result<BackupCreateResponse> createResult = supplierCreateInvConfig.get();

    assertThat(createResult).isNotNull()
        .extracting(Result::getError).isNotNull()
        .extracting(WeaviateError::getMessages)
        .satisfies(errors -> assertThat(errors.stream().filter(m -> m.getMessage().contains("CPUPercentage")).count())
            .isGreaterThanOrEqualTo(1));

    // Create backup
    createResult = supplierCreate.get();

    assertThat(createResult.hasErrors()).isFalse();
    assertThat(createResult.getResult()).isNotNull()
        .returns(backupId, BackupCreateResponse::getId)
        .returns(new String[] { CLASS_NAME_PIZZA }, BackupCreateResponse::getClassNames)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateResponse::getPath)
        .returns(BACKEND, BackupCreateResponse::getBackend)
        .returns(CreateStatus.SUCCESS, BackupCreateResponse::getStatus)
        .returns(null, BackupCreateResponse::getError);

    assertThatAllPizzasExist(supplierGQLOfClass);

    // Check backup status
    Result<BackupCreateStatusResponse> createStatusResult = supplierCreateStatus.get();

    assertThat(createStatusResult.hasErrors()).isFalse();
    assertThat(createStatusResult.getResult()).isNotNull()
        .returns(backupId, BackupCreateStatusResponse::getId)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateStatusResponse::getPath)
        .returns(BACKEND, BackupCreateStatusResponse::getBackend)
        .returns(CreateStatus.SUCCESS, BackupCreateStatusResponse::getStatus)
        .returns(null, BackupCreateStatusResponse::getError);

    // Remove existing class
    Result<Boolean> delete = supplierDeleteClass.get();

    assertThat(delete.hasErrors()).isFalse();
    assertThat(delete.getResult()).isTrue();

    // Try to restore with bad restore config
    Result<BackupRestoreResponse> restoreResult = supplierRestoreInvConfig.get();

    assertThat(restoreResult).isNotNull()
        .extracting(Result::getError).isNotNull()
        .extracting(WeaviateError::getMessages).isNotNull()
        .satisfies(errors -> assertThat(errors.stream().filter(m -> m.getMessage().contains("CPUPercentage")).count())
            .isGreaterThanOrEqualTo(1));

    // Restore backup
    restoreResult = supplierRestore.get();

    assertThat(restoreResult.hasErrors()).isFalse();
    assertThat(restoreResult.getResult()).isNotNull()
        .returns(backupId, BackupRestoreResponse::getId)
        .returns(new String[] { CLASS_NAME_PIZZA }, BackupRestoreResponse::getClassNames)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreResponse::getPath)
        .returns(BACKEND, BackupRestoreResponse::getBackend)
        .returns(RestoreStatus.SUCCESS, BackupRestoreResponse::getStatus)
        .returns(null, BackupRestoreResponse::getError);

    assertThatAllPizzasExist(supplierGQLOfClass);

    // Check restore backup
    Result<BackupRestoreStatusResponse> restoreStatusResult = supplierRestoreStatus.get();

    assertThat(restoreStatusResult.hasErrors()).isFalse();
    assertThat(restoreStatusResult.getResult()).isNotNull()
        .returns(backupId, BackupRestoreStatusResponse::getId)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreStatusResponse::getPath)
        .returns(BACKEND, BackupRestoreStatusResponse::getBackend)
        .returns(RestoreStatus.SUCCESS, BackupRestoreStatusResponse::getStatus)
        .returns(null, BackupRestoreStatusResponse::getError);
  }

  public static void testCancelBackup(Supplier<Result<BackupCreateResponse>> supplierCreate,
      Supplier<Result<Void>> supplierCancel,
      Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatus) {
    Result<BackupCreateResponse> createResult = supplierCreate.get();
    assertThat(createResult.getError()).as("start backup").isNull();

    Result<Void> cancelResult = supplierCancel.get();
    assertThat(cancelResult.getError()).as("cancel backup").isNull();

    waitForCreateStatus(supplierCreateStatus, CreateStatus.CANCELED);
  }

  public static void testBackupRestoreWithRbacOptions(String backupId,
      Runnable arrange,
      Runnable delete,
      Supplier<Result<BackupCreateResponse>> supplierCreate,
      Supplier<Result<BackupRestoreResponse>> supplierRestore,
      Supplier<Result<UserDb>> supplierUser,
      Supplier<Result<Role>> supplierRole) {

    // Arrange
    arrange.run();

    // Act
    Result<BackupCreateResponse> createBackup = supplierCreate.get();
    assertThat(createBackup.getError()).as("create backup").isNull();
    assertThat(createBackup.getResult().getStatus())
        .as("create backup status: " + createBackup.getResult().getError()).isEqualTo("SUCCESS");

    delete.run();

    Result<BackupRestoreResponse> restoreBackup = supplierRestore.get();
    assertThat(restoreBackup.getError()).as("restore backup").isNull();
    assertThat(restoreBackup.getResult().getStatus())
        .as("restore backup status: " + restoreBackup.getResult().getError()).isEqualTo("SUCCESS");

    // Assert
    assertThat(supplierUser.get().getResult()).as("get restored user").isNotNull();
    assertThat(supplierRole.get().getResult()).as("get restored role").isNotNull();

  }

  public static void testOverwriteAlias_true(
      Runnable arrange,
      Callable<Result<?>> act,
      Supplier<Alias> supplierAlias, String wantClassName) throws Exception {
    arrange.run();
    Result<?> result = act.call();
    assertThat(result.getError()).isNull();
    assertThat(supplierAlias.get().getClassName()).isEqualTo(wantClassName);
  }

  private static void assertThatAllPizzasExist(Function<String, Result<GraphQLResponse>> supplierGQLOfClass) {
    assertThatAllFoodObjectsExist(supplierGQLOfClass, "Pizza", "Quattro Formaggi", "Frutti di Mare", "Hawaii",
        "Doener");
  }

  private static void assertThatAllSoupsExist(Function<String, Result<GraphQLResponse>> supplierGQLOfClass) {
    assertThatAllFoodObjectsExist(supplierGQLOfClass, "Soup", "ChickenSoup", "Beautiful");
  }

  private static void assertThatAllFoodObjectsExist(Function<String, Result<GraphQLResponse>> supplierGQLOfClass,
      String className, String... names) {
    Result<GraphQLResponse> result = supplierGQLOfClass.apply(className);

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
   * Periodically polls backup creation status until it reaches the desired
   * ({@code want}) state or the deadline expires.
   *
   * <br>
   * Interval: 100ms
   * <br>
   * Timeout: 5s
   */
  private static void waitForCreateStatus(Supplier<Result<BackupCreateStatusResponse>> supplierCreateStatus,
      String want) {
    final int MAX_RETRIES = 5_000 / 100;
    AtomicReference<String> status = new AtomicReference<>("");

    Callable<Boolean> statusCheck = () -> {
      Result<BackupCreateStatusResponse> check = supplierCreateStatus.get();
      String current = check.getResult().getStatus();
      status.set(current);
      return current.equalsIgnoreCase(want);
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

  /**
   * Skip a test if the operation is not implemented on the server.
   *
   * <p>
   * We assume that in such cases the server will return an response with body
   * "not implemented";
   * this is not a good reason to fail the client's test.
   *
   * @param result Any Result object from a request.
   */
  private static void skipIfNotImplemented(Result<?> result) {
    if (result.hasErrors()) {
      assumeTrue(
          "this operation is not implemented on the server",
          result.getError().getMessages().stream()
              .noneMatch(err -> err.getMessage().toLowerCase().contains("not implemented")));
    }
  }
}
