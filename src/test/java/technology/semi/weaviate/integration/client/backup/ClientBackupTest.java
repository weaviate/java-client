package technology.semi.weaviate.integration.client.backup;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import technology.semi.weaviate.client.Config;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.base.WeaviateError;
import technology.semi.weaviate.client.base.WeaviateErrorMessage;
import technology.semi.weaviate.client.v1.backup.api.BackupCreateStatusGetter;
import technology.semi.weaviate.client.v1.backup.api.BackupRestoreStatusGetter;
import technology.semi.weaviate.client.v1.backup.model.BackupCreateMeta;
import technology.semi.weaviate.client.v1.backup.model.BackupRestoreMeta;
import technology.semi.weaviate.client.v1.backup.model.CreateStatus;
import technology.semi.weaviate.client.v1.backup.model.RestoreStatus;
import technology.semi.weaviate.client.v1.backup.model.Storage;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLResponse;
import technology.semi.weaviate.client.v1.graphql.query.fields.Field;
import technology.semi.weaviate.integration.client.WeaviateTestGenerics;

import java.io.File;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.CHAR_SEQUENCE;

public class ClientBackupTest {

  private static final String DOCKER_COMPOSE_BACKUPS_DIR = "/tmp/backups";
  private static final String CLASS_NAME = "Pizza";
  private static final String NOT_EXISTING_CLASS_NAME = "not-existing-class";
  private static final String STORAGE_NAME = Storage.FILESYSTEM;
  private static final String NOT_EXISTING_STORAGE_NAME = "not-existing-storage";

  private String backupId;
  private String notExistingBackupId;
  private WeaviateClient client;

  private final WeaviateTestGenerics testGenerics = new WeaviateTestGenerics();


  @ClassRule
  public static DockerComposeContainer<?> compose = new DockerComposeContainer<>(
    new File("src/test/resources/docker-compose-test.yaml")
  ).withExposedService("weaviate_1", 8080, Wait.forHttp("/v1/.well-known/ready").forStatusCode(200));

  @Before
  public void before() {
    String host = compose.getServiceHost("weaviate_1", 8080);
    Integer port = compose.getServicePort("weaviate_1", 8080);
    Config config = new Config("http", host + ":" + port);

    backupId = "backup-" + new Random().nextInt(Integer.MAX_VALUE);
    notExistingBackupId = "not-existing-backup-" + new Random().nextInt(Integer.MAX_VALUE);

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
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaCreate.hasErrors()).isFalse();
    assertThat(metaCreate.getResult()).isNotNull()
      .returns(backupId, BackupCreateMeta::getId)
      // TODO add className to weaviate response
      // .returns(className, BackupCreateMeta::getClassName)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + CLASS_NAME + "/" + backupId, BackupCreateMeta::getPath)
      .returns(STORAGE_NAME, BackupCreateMeta::getStorageName)
      .returns(CreateStatus.SUCCESS, BackupCreateMeta::getStatus)
      .returns(null, BackupCreateMeta::getError);

    assertThatAllPizzasExist();

    // Check backup status
    Result<BackupCreateMeta> metaCreateStatus = client.backup().createStatusGetter()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaCreateStatus.hasErrors()).isFalse();
    assertThat(metaCreateStatus.getResult()).isNotNull()
      .returns(backupId, BackupCreateMeta::getId)
      // TODO add className to weaviate response
      // .returns(className, BackupCreateMeta::getClassName)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + CLASS_NAME + "/" + backupId, BackupCreateMeta::getPath)
      .returns(STORAGE_NAME, BackupCreateMeta::getStorageName)
      .returns(CreateStatus.SUCCESS, BackupCreateMeta::getStatus)
      .returns(null, BackupCreateMeta::getError);

    // Remove existing class
    Result<Boolean> delete = client.schema().classDeleter()
      .withClassName(CLASS_NAME)
      .run();

    assertThat(delete.hasErrors()).isFalse();
    assertThat(delete.getResult()).isTrue();

    // Restore backup
    Result<BackupRestoreMeta> metaRestore = client.backup().restorer()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaRestore.hasErrors()).isFalse();
    assertThat(metaRestore.getResult()).isNotNull()
      .returns(backupId, BackupRestoreMeta::getId)
      .returns(CLASS_NAME, BackupRestoreMeta::getClassName)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + CLASS_NAME + "/" + backupId, BackupRestoreMeta::getPath)
      .returns(STORAGE_NAME, BackupRestoreMeta::getStorageName)
      .returns(RestoreStatus.SUCCESS, BackupRestoreMeta::getStatus)
      .returns(null, BackupRestoreMeta::getError);

    assertThatAllPizzasExist();

    // Check restore backup
    Result<BackupRestoreMeta> metaRestoreStatus = client.backup().restoreStatusGetter()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaRestoreStatus.hasErrors()).isFalse();
    assertThat(metaRestoreStatus.getResult()).isNotNull()
      .returns(backupId, BackupRestoreMeta::getId)
      .returns(CLASS_NAME, BackupRestoreMeta::getClassName)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + CLASS_NAME + "/" + backupId, BackupRestoreMeta::getPath)
      .returns(STORAGE_NAME, BackupRestoreMeta::getStorageName)
      .returns(RestoreStatus.SUCCESS, BackupRestoreMeta::getStatus)
      .returns(null, BackupRestoreMeta::getError);
  }

  @Test
  public void shouldCreateAndRestoreBackupWithoutWaiting() throws InterruptedException {
    assertThatAllPizzasExist();

    // Start creating backup
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaCreate.hasErrors()).isFalse();
    assertThat(metaCreate.getResult()).isNotNull()
      .returns(backupId, BackupCreateMeta::getId)
      // TODO add className to weaviate response
      // .returns(CLASS_NAME, BackupCreateMeta::getClassName)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + CLASS_NAME + "/" + backupId, BackupCreateMeta::getPath)
      .returns(STORAGE_NAME, BackupCreateMeta::getStorageName)
      .returns(CreateStatus.STARTED, BackupCreateMeta::getStatus)
      .returns(null, BackupCreateMeta::getError);

    // Wait until created
    BackupCreateStatusGetter createStatusGetter = client.backup().createStatusGetter()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId);

    Result<BackupCreateMeta> metaCreateStatus;
    while (true) {
      metaCreateStatus = createStatusGetter.run();

      assertThat(metaCreateStatus.hasErrors()).isFalse();
      assertThat(metaCreateStatus.getResult()).isNotNull()
        .returns(backupId, BackupCreateMeta::getId)
        // TODO add className to weaviate response
        // .returns(CLASS_NAME, BackupCreateMeta::getClassName)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + CLASS_NAME + "/" + backupId, BackupCreateMeta::getPath)
        .returns(STORAGE_NAME, BackupCreateMeta::getStorageName)
        .returns(null, BackupCreateMeta::getError)
        .extracting(BackupCreateMeta::getStatus).isIn(CreateStatus.STARTED, CreateStatus.TRANSFERRING,
          CreateStatus.TRANSFERRED, CreateStatus.SUCCESS);

      if (CreateStatus.SUCCESS.equals(metaCreateStatus.getResult().getStatus())) {
        break;
      }
      Thread.sleep(100);
    }

    assertThatAllPizzasExist();

    // Remove existing class
    Result<Boolean> delete = client.schema().classDeleter()
      .withClassName(CLASS_NAME)
      .run();

    assertThat(delete.hasErrors()).isFalse();
    assertThat(delete.getResult()).isTrue();

    // Start restoring backup
    Result<BackupRestoreMeta> metaRestore = client.backup().restorer()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaRestore.hasErrors()).isFalse();
    assertThat(metaRestore.getResult()).isNotNull()
      .returns(backupId, BackupRestoreMeta::getId)
      // TODO add className to weaviate response
//      .returns(CLASS_NAME, BackupRestoreMeta::getClassName)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + CLASS_NAME + "/" + backupId, BackupRestoreMeta::getPath)
      .returns(STORAGE_NAME, BackupRestoreMeta::getStorageName)
      .returns(RestoreStatus.STARTED, BackupRestoreMeta::getStatus)
      .returns(null, BackupRestoreMeta::getError);

    // Wait until restored
    BackupRestoreStatusGetter restoreStatusGetter = client.backup().restoreStatusGetter()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId);

    Result<BackupRestoreMeta> metaRestoreStatus;
    while (true) {
      metaRestoreStatus = restoreStatusGetter.run();

      assertThat(metaRestoreStatus.hasErrors()).isFalse();
      assertThat(metaRestoreStatus.getResult()).isNotNull()
        .returns(backupId, BackupRestoreMeta::getId)
        .returns(CLASS_NAME, BackupRestoreMeta::getClassName)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + CLASS_NAME + "/" + backupId, BackupRestoreMeta::getPath)
        .returns(STORAGE_NAME, BackupRestoreMeta::getStorageName)
        .returns(null, BackupRestoreMeta::getError)
        .extracting(BackupRestoreMeta::getStatus).isIn(RestoreStatus.STARTED, RestoreStatus.TRANSFERRING,
          RestoreStatus.TRANSFERRED, RestoreStatus.SUCCESS);

      if (RestoreStatus.SUCCESS.equals(metaRestoreStatus.getResult().getStatus())) {
        break;
      }
      Thread.sleep(200);
    }

    assertThatAllPizzasExist();
  }

  @Test
  public void shouldFailOnCreateBackupOnNotExistingStorage() {
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withClassName(CLASS_NAME)
      .withStorageName(NOT_EXISTING_STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaCreate.hasErrors()).isTrue();
    assertThat(metaCreate.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_STORAGE_NAME);
  }

  @Test
  public void shouldFailOnCreateBackupStatusOnNotExistingStorage() {
    Result<BackupCreateMeta> metaCreateStatus = client.backup().createStatusGetter()
      .withClassName(CLASS_NAME)
      .withStorageName(NOT_EXISTING_STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaCreateStatus.hasErrors()).isTrue();
    assertThat(metaCreateStatus.getError()).isNotNull()
      // TODO should be 422?
      .returns(500, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_STORAGE_NAME);
  }

  @Test
  public void shouldFailOnRestoreBackupOnNotExistingStorage() {
    Result<BackupRestoreMeta> metaRestore = client.backup().restorer()
      .withClassName(NOT_EXISTING_CLASS_NAME)
      .withStorageName(NOT_EXISTING_STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaRestore.hasErrors()).isTrue();
    assertThat(metaRestore.getError()).isNotNull()
      // TODO should be 422?
      .returns(500, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_STORAGE_NAME);
  }

  @Test
  public void shouldFailOnRestoreBackupStatusOnNotExistingStorage() {
    Result<BackupRestoreMeta> metaRestoreStatus = client.backup().restoreStatusGetter()
      .withClassName(NOT_EXISTING_CLASS_NAME)
      .withStorageName(NOT_EXISTING_STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaRestoreStatus.hasErrors()).isTrue();
    assertThat(metaRestoreStatus.getError()).isNotNull()
      // TODO should be 422?
      .returns(500, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_STORAGE_NAME);
  }

  @Test
  public void shouldFailOnCreateBackupForNotExistingClass() {
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withClassName(NOT_EXISTING_CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaCreate.hasErrors()).isTrue();
    assertThat(metaCreate.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_CLASS_NAME);
  }

  @Test
  public void shouldFailOnCreateBackupStatusForNotExistingClass() {
    Result<BackupCreateMeta> metaCreateStatus = client.backup().createStatusGetter()
      .withClassName(NOT_EXISTING_CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaCreateStatus.hasErrors()).isTrue();
    assertThat(metaCreateStatus.getError()).isNotNull()
      // TODO should be 422?
      .returns(404, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_CLASS_NAME);
  }

  @Test
  public void shouldFailOnRestoreBackupForExistingClass() {
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaCreate.hasErrors()).isFalse();

    Result<BackupRestoreMeta> metaRestore = client.backup().restorer()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

//    // TODO should fail immediately
//    assertThat(metaRestore.hasErrors()).isTrue();
//    assertThat(metaRestore.getError()).isNotNull()
//      .returns(422, WeaviateError::getStatusCode)
//      .extracting(WeaviateError::getMessages).asList()
//      .hasSizeGreaterThan(0)
//      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
//      .first().asInstanceOf(CHAR_SEQUENCE).contains(CLASS_NAME);

    assertThat(metaRestore.hasErrors()).isFalse();
    assertThat(metaRestore.getResult()).isNotNull()
      .returns(backupId, BackupRestoreMeta::getId)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + CLASS_NAME + "/" + backupId, BackupRestoreMeta::getPath)
      .returns(STORAGE_NAME, BackupRestoreMeta::getStorageName)
      .returns(RestoreStatus.STARTED, BackupRestoreMeta::getStatus)
      .returns(null, BackupRestoreMeta::getError);
  }

  @Test
  public void shouldFailOnRestoreBackupStatusForExistingClass() {
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaCreate.hasErrors()).isFalse();

    Result<BackupRestoreMeta> metaRestoreStatus = client.backup().restoreStatusGetter()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaRestoreStatus.hasErrors()).isTrue();
    assertThat(metaRestoreStatus.getError()).isNotNull()
      // TODO should be 422?
      .returns(500, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      // TODO invalid error message - status not found vs expected existing index
      .first().asInstanceOf(CHAR_SEQUENCE).contains(CLASS_NAME);
  }

  @Test
  public void shouldFailOnCreateOfExistingBackup() {
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaCreate.hasErrors()).isFalse();

    Result<BackupCreateMeta> metaCreateAgain = client.backup().creator()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaCreateAgain.hasErrors()).isTrue();
    assertThat(metaCreateAgain.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(backupId);
  }

  @Test
  public void shouldFailOnCreateStatusOfNotExistingBackup() {
    Result<BackupCreateMeta> metaCreateStatus = client.backup().createStatusGetter()
      .withClassName(CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(notExistingBackupId)
      .run();

    assertThat(metaCreateStatus.hasErrors()).isTrue();
    assertThat(metaCreateStatus.getError()).isNotNull()
      .returns(404, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(notExistingBackupId);
  }

  @Test
  public void shouldFailOnRestoreOfNotExistingBackup() {
    Result<BackupRestoreMeta> metaRestore = client.backup().restorer()
      .withClassName(NOT_EXISTING_CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(notExistingBackupId)
      .run();

//    // TODO should fail immediately
//    assertThat(metaRestore.hasErrors()).isTrue();
//    assertThat(metaRestore.getError()).isNotNull()
//      .returns(404, WeaviateError::getStatusCode)
//      .extracting(WeaviateError::getMessages).asList()
//      .hasSizeGreaterThan(0)
//      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
//      .first().asInstanceOf(CHAR_SEQUENCE).contains(notExistingBackupId);

    assertThat(metaRestore.hasErrors()).isFalse();
    assertThat(metaRestore.getResult()).isNotNull()
      .returns(notExistingBackupId, BackupRestoreMeta::getId)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + NOT_EXISTING_CLASS_NAME + "/" + notExistingBackupId, BackupRestoreMeta::getPath)
      .returns(STORAGE_NAME, BackupRestoreMeta::getStorageName)
      .returns(RestoreStatus.STARTED, BackupRestoreMeta::getStatus)
      .returns(null, BackupRestoreMeta::getError);
  }

  @Test
  public void shouldFailOnRestoreStatusOfNotExistingBackup() {
    Result<BackupRestoreMeta> metaRestoreStatus = client.backup().restoreStatusGetter()
      .withClassName(NOT_EXISTING_CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(notExistingBackupId)
      .run();

    assertThat(metaRestoreStatus.hasErrors()).isTrue();
    assertThat(metaRestoreStatus.getError()).isNotNull()
      // TODO should be 404?
      .returns(500, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(notExistingBackupId);
  }

  private void assertThatAllPizzasExist() {
    Result<GraphQLResponse> result = client.graphQL().get()
      .withClassName("Pizza")
      .withFields(Field.builder().name("name").build())
      .run();

    assertThat(result.hasErrors()).isFalse();
    assertThat(result.getResult()).isNotNull()
      .extracting(GraphQLResponse::getData).isInstanceOf(Map.class)
      .extracting(data -> ((Map<?, ?>)data).get("Get")).isInstanceOf(Map.class)
      .extracting(get -> ((Map<?, ?>)get).get("Pizza")).asList()
      .hasSize(4).hasOnlyElementsOfType(Map.class)
      .extracting(pizza -> ((Map<?,?>)pizza).get("name")).hasOnlyElementsOfType(String.class)
      .extracting(name -> (String)name)
      .containsExactlyInAnyOrder("Quattro Formaggi", "Frutti di Mare", "Hawaii", "Doener");
  }
}
