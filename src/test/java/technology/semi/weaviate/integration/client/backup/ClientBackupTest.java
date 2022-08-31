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
  private static final String CLASS_NAME_PIZZA = "Pizza";
  private static final String CLASS_NAME_SOUP = "Soup";
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
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaCreate.hasErrors()).isFalse();
    assertThat(metaCreate.getResult()).isNotNull()
      .returns(backupId, BackupCreateMeta::getId)
      .returns(new String[]{CLASS_NAME_PIZZA}, BackupCreateMeta::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateMeta::getPath)
      .returns(STORAGE_NAME, BackupCreateMeta::getStorageName)
      .returns(CreateStatus.SUCCESS, BackupCreateMeta::getStatus)
      .returns(null, BackupCreateMeta::getError);

    assertThatAllPizzasExist();

    // Check backup status
    Result<BackupCreateMeta> metaCreateStatus = client.backup().createStatusGetter()
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaCreateStatus.hasErrors()).isFalse();
    assertThat(metaCreateStatus.getResult()).isNotNull()
      .returns(backupId, BackupCreateMeta::getId)
      .returns(new String[]{CLASS_NAME_PIZZA}, BackupCreateMeta::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateMeta::getPath)
      .returns(STORAGE_NAME, BackupCreateMeta::getStorageName)
      .returns(CreateStatus.SUCCESS, BackupCreateMeta::getStatus)
      .returns(null, BackupCreateMeta::getError);

    // Remove existing class
    Result<Boolean> delete = client.schema().classDeleter()
      .withClassName(CLASS_NAME_PIZZA)
      .run();

    assertThat(delete.hasErrors()).isFalse();
    assertThat(delete.getResult()).isTrue();

    // Restore backup
    Result<BackupRestoreMeta> metaRestore = client.backup().restorer()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaRestore.hasErrors()).isFalse();
    assertThat(metaRestore.getResult()).isNotNull()
      .returns(backupId, BackupRestoreMeta::getId)
      .returns(new String[]{CLASS_NAME_PIZZA}, BackupRestoreMeta::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreMeta::getPath)
      .returns(STORAGE_NAME, BackupRestoreMeta::getStorageName)
      .returns(RestoreStatus.SUCCESS, BackupRestoreMeta::getStatus)
      .returns(null, BackupRestoreMeta::getError);

    assertThatAllPizzasExist();

    // Check restore backup
    Result<BackupRestoreMeta> metaRestoreStatus = client.backup().restoreStatusGetter()
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaRestoreStatus.hasErrors()).isFalse();
    assertThat(metaRestoreStatus.getResult()).isNotNull()
      .returns(backupId, BackupRestoreMeta::getId)
      .returns(new String[]{CLASS_NAME_PIZZA}, BackupRestoreMeta::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreMeta::getPath)
      .returns(STORAGE_NAME, BackupRestoreMeta::getStorageName)
      .returns(RestoreStatus.SUCCESS, BackupRestoreMeta::getStatus)
      .returns(null, BackupRestoreMeta::getError);
  }

  @Test
  public void shouldCreateAndRestoreBackupWithoutWaiting() throws InterruptedException {
    assertThatAllPizzasExist();

    // Start creating backup
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaCreate.hasErrors()).isFalse();
    assertThat(metaCreate.getResult()).isNotNull()
      .returns(backupId, BackupCreateMeta::getId)
      .returns(new String[]{CLASS_NAME_PIZZA}, BackupCreateMeta::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateMeta::getPath)
      .returns(STORAGE_NAME, BackupCreateMeta::getStorageName)
      .returns(CreateStatus.STARTED, BackupCreateMeta::getStatus)
      .returns(null, BackupCreateMeta::getError);

    // Wait until created
    BackupCreateStatusGetter createStatusGetter = client.backup().createStatusGetter()
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId);

    Result<BackupCreateMeta> metaCreateStatus;
    while (true) {
      metaCreateStatus = createStatusGetter.run();

      assertThat(metaCreateStatus.hasErrors()).isFalse();
      assertThat(metaCreateStatus.getResult()).isNotNull()
        .returns(backupId, BackupCreateMeta::getId)
        .returns(new String[]{CLASS_NAME_PIZZA}, BackupCreateMeta::getClassNames)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateMeta::getPath)
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
      .withClassName(CLASS_NAME_PIZZA)
      .run();

    assertThat(delete.hasErrors()).isFalse();
    assertThat(delete.getResult()).isTrue();

    // Start restoring backup
    Result<BackupRestoreMeta> metaRestore = client.backup().restorer()
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaRestore.hasErrors()).isFalse();
    assertThat(metaRestore.getResult()).isNotNull()
      .returns(backupId, BackupRestoreMeta::getId)
      .returns(new String[]{CLASS_NAME_PIZZA}, BackupRestoreMeta::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreMeta::getPath)
      .returns(STORAGE_NAME, BackupRestoreMeta::getStorageName)
      .returns(RestoreStatus.STARTED, BackupRestoreMeta::getStatus)
      .returns(null, BackupRestoreMeta::getError);

    // Wait until restored
    BackupRestoreStatusGetter restoreStatusGetter = client.backup().restoreStatusGetter()
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId);

    Result<BackupRestoreMeta> metaRestoreStatus;
    while (true) {
      metaRestoreStatus = restoreStatusGetter.run();

      assertThat(metaRestoreStatus.hasErrors()).isFalse();
      assertThat(metaRestoreStatus.getResult()).isNotNull()
        .returns(backupId, BackupRestoreMeta::getId)
        .returns(new String[]{CLASS_NAME_PIZZA}, BackupRestoreMeta::getClassNames)
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreMeta::getPath)
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
  public void shouldCreateAndRestore1Of2Classes() {
    assertThatAllPizzasExist();
    assertThatAllSoupsExist();

    // Create backup
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA, CLASS_NAME_SOUP)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaCreate.hasErrors()).isFalse();
    assertThat(metaCreate.getResult()).isNotNull()
      .returns(backupId, BackupCreateMeta::getId)
      .returns(new String[]{CLASS_NAME_PIZZA, CLASS_NAME_SOUP}, BackupCreateMeta::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateMeta::getPath)
      .returns(STORAGE_NAME, BackupCreateMeta::getStorageName)
      .returns(CreateStatus.SUCCESS, BackupCreateMeta::getStatus)
      .returns(null, BackupCreateMeta::getError);

    assertThatAllPizzasExist();
    assertThatAllSoupsExist();

    // Check backup status
    Result<BackupCreateMeta> metaCreateStatus = client.backup().createStatusGetter()
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaCreateStatus.hasErrors()).isFalse();
    assertThat(metaCreateStatus.getResult()).isNotNull()
      .returns(backupId, BackupCreateMeta::getId)
      .returns(new String[]{CLASS_NAME_PIZZA, CLASS_NAME_SOUP}, BackupCreateMeta::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupCreateMeta::getPath)
      .returns(STORAGE_NAME, BackupCreateMeta::getStorageName)
      .returns(CreateStatus.SUCCESS, BackupCreateMeta::getStatus)
      .returns(null, BackupCreateMeta::getError);

    // Remove existing class
    Result<Boolean> delete = client.schema().classDeleter()
      .withClassName(CLASS_NAME_PIZZA)
      .run();

    assertThat(delete.hasErrors()).isFalse();
    assertThat(delete.getResult()).isTrue();

    // Restore backup
    Result<BackupRestoreMeta> metaRestore = client.backup().restorer()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaRestore.hasErrors()).isFalse();
    assertThat(metaRestore.getResult()).isNotNull()
      .returns(backupId, BackupRestoreMeta::getId)
      .returns(new String[]{CLASS_NAME_PIZZA}, BackupRestoreMeta::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreMeta::getPath)
      .returns(STORAGE_NAME, BackupRestoreMeta::getStorageName)
      .returns(RestoreStatus.SUCCESS, BackupRestoreMeta::getStatus)
      .returns(null, BackupRestoreMeta::getError);

    assertThatAllPizzasExist();
    assertThatAllSoupsExist();

    // Check restore backup
    Result<BackupRestoreMeta> metaRestoreStatus = client.backup().restoreStatusGetter()
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaRestoreStatus.hasErrors()).isFalse();
    assertThat(metaRestoreStatus.getResult()).isNotNull()
      .returns(backupId, BackupRestoreMeta::getId)
      .returns(new String[]{CLASS_NAME_PIZZA}, BackupRestoreMeta::getClassNames)
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId, BackupRestoreMeta::getPath)
      .returns(STORAGE_NAME, BackupRestoreMeta::getStorageName)
      .returns(RestoreStatus.SUCCESS, BackupRestoreMeta::getStatus)
      .returns(null, BackupRestoreMeta::getError);
  }

  @Test
  public void shouldFailOnCreateBackupOnNotExistingStorage() {
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
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
      .withStorageName(NOT_EXISTING_STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaCreateStatus.hasErrors()).isTrue();
    assertThat(metaCreateStatus.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_STORAGE_NAME);
  }

  @Test
  public void shouldFailOnRestoreBackupFromNotExistingStorage() {
    Result<BackupRestoreMeta> metaRestore = client.backup().restorer()
      .withIncludeClassNames(NOT_EXISTING_CLASS_NAME)
      .withStorageName(NOT_EXISTING_STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaRestore.hasErrors()).isTrue();
    assertThat(metaRestore.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_STORAGE_NAME);
  }

  @Test
  public void shouldFailOnCreateBackupForNotExistingClass() {
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withIncludeClassNames(NOT_EXISTING_CLASS_NAME)
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
  public void shouldFailOnRestoreBackupForExistingClass() {
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaCreate.hasErrors()).isFalse();

    Result<BackupRestoreMeta> metaRestore = client.backup().restorer()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaRestore.hasErrors()).isTrue();
    assertThat(metaRestore.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(CLASS_NAME_PIZZA);
  }

  @Test
  public void shouldFailOnCreateOfExistingBackup() {
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaCreate.hasErrors()).isFalse();

    Result<BackupCreateMeta> metaCreateAgain = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
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
      .withIncludeClassNames(NOT_EXISTING_CLASS_NAME)
      .withStorageName(STORAGE_NAME)
      .withBackupId(notExistingBackupId)
      .run();

    assertThat(metaRestore.hasErrors()).isTrue();
    assertThat(metaRestore.getError()).isNotNull()
      .returns(404, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(notExistingBackupId);
  }

  @Test
  public void shouldFailOnRestoreBackupStatusOfNotStartedRestore() {
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaCreate.hasErrors()).isFalse();

    Result<BackupRestoreMeta> metaRestoreStatus = client.backup().restoreStatusGetter()
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaRestoreStatus.hasErrors()).isTrue();
    assertThat(metaRestoreStatus.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      // TODO adjust to error message
      .first().asInstanceOf(CHAR_SEQUENCE).contains(STORAGE_NAME).contains(backupId);
  }

  @Test
  public void shouldFailOnCreateBackupForBothIncludeAndExcludeClasses() {
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withExcludeClassNames(CLASS_NAME_SOUP)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaCreate.hasErrors()).isTrue();
    assertThat(metaCreate.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      // TODO adjust to error message
      .first().asInstanceOf(CHAR_SEQUENCE).contains(CLASS_NAME_PIZZA).contains(CLASS_NAME_SOUP);
  }

  @Test
  public void shouldFailOnRestoreBackupForBothIncludeAndExcludeClasses() {
    // Create backup
    Result<BackupCreateMeta> metaCreate = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA, CLASS_NAME_SOUP)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(metaCreate.hasErrors()).isFalse();

    // Remove existing class
    Result<Boolean> delete = client.schema().classDeleter()
      .withClassName(CLASS_NAME_PIZZA)
      .run();

    assertThat(delete.hasErrors()).isFalse();

    // Restore
    Result<BackupRestoreMeta> metaRestore = client.backup().restorer()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withExcludeClassNames(CLASS_NAME_SOUP)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId)
      .run();

    assertThat(metaRestore.hasErrors()).isTrue();
    assertThat(metaRestore.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      // TODO adjust to error message
      .first().asInstanceOf(CHAR_SEQUENCE).contains(CLASS_NAME_PIZZA).contains(CLASS_NAME_SOUP);
  }

  @Test
  public void shouldGetAllExistingBackups() {
    Result<BackupCreateMeta> metaCreatePizza = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId + "Pizza")
      .withWaitForCompletion(true)
      .run();

    assertThat(metaCreatePizza.hasErrors()).isFalse();

    Result<BackupCreateMeta> metaCreateSoup = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_SOUP)
      .withStorageName(STORAGE_NAME)
      .withBackupId(backupId + "Soup")
      .withWaitForCompletion(true)
      .run();

    assertThat(metaCreateSoup.hasErrors()).isFalse();

    Result<BackupCreateMeta[]> metas = client.backup().getter()
      .withStorageName(STORAGE_NAME)
      .run();

    assertThat(metas.hasErrors()).isFalse();
    assertThat(metas.getResult()).isNotNull()
      .hasSize(2)
      .extracting(BackupCreateMeta::getId)
      .containsExactlyInAnyOrder(backupId + "Pizza", backupId + "Soup");
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
      .extracting(data -> ((Map<?, ?>)data).get("Get")).isInstanceOf(Map.class)
      .extracting(get -> ((Map<?, ?>)get).get(className)).asList()
      .hasSize(names.length).hasOnlyElementsOfType(Map.class)
      .extracting(pizza -> ((Map<?,?>)pizza).get("name")).hasOnlyElementsOfType(String.class)
      .extracting(name -> (String)name)
      .containsExactlyInAnyOrder(names);
  }
}
