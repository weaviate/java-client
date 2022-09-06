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
import technology.semi.weaviate.client.v1.backup.model.BackupCreateResponse;
import technology.semi.weaviate.client.v1.backup.model.BackupCreateStatusResponse;
import technology.semi.weaviate.client.v1.backup.model.BackupRestoreResponse;
import technology.semi.weaviate.client.v1.backup.model.BackupRestoreStatusResponse;
import technology.semi.weaviate.client.v1.backup.model.CreateStatus;
import technology.semi.weaviate.client.v1.backup.model.RestoreStatus;
import technology.semi.weaviate.client.v1.backup.model.Backend;
import technology.semi.weaviate.client.v1.graphql.model.GraphQLResponse;
import technology.semi.weaviate.client.v1.graphql.query.fields.Field;
import technology.semi.weaviate.integration.client.WeaviateTestGenerics;

import java.io.File;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;
import static org.assertj.core.api.InstanceOfAssertFactories.CHAR_SEQUENCE;

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
    Result<BackupCreateResponse> createResult = client.backup().creator()
      .withIncludeClassNames(CLASS_NAME_PIZZA)
      .withBackend(BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(createResult.hasErrors()).isFalse();
    assertThat(createResult.getResult()).isNotNull()
      .returns(backupId, BackupCreateResponse::getId)
      .returns(new String[]{CLASS_NAME_PIZZA}, BackupCreateResponse::getClassNames)
      // TODO remove/leave /snapshot.json
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId + "/snapshot.json", BackupCreateResponse::getPath)
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
      // TODO remove/leave /snapshot.json
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId + "/snapshot.json", BackupCreateStatusResponse::getPath)
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
      .backend(BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(restoreResult.hasErrors()).isFalse();
    assertThat(restoreResult.getResult()).isNotNull()
      .returns(backupId, BackupRestoreResponse::getId)
      .returns(new String[]{CLASS_NAME_PIZZA}, BackupRestoreResponse::getClassNames)
      // TODO remove/leave /snapshot.json
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId + "/snapshot.json", BackupRestoreResponse::getPath)
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
      // TODO remove/leave /snapshot.json
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId + "/snapshot.json", BackupRestoreStatusResponse::getPath)
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

    assertThat(createResult.hasErrors()).isFalse();
    assertThat(createResult.getResult()).isNotNull()
      .returns(backupId, BackupCreateResponse::getId)
      .returns(new String[]{CLASS_NAME_PIZZA}, BackupCreateResponse::getClassNames)
      // TODO remove/leave /snapshot.json
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId + "/snapshot.json", BackupCreateResponse::getPath)
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

      assertThat(createStatusResult.hasErrors()).isFalse();
      assertThat(createStatusResult.getResult()).isNotNull()
        .returns(backupId, BackupCreateStatusResponse::getId)
        // TODO remove/leave /snapshot.json
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId + "/snapshot.json", BackupCreateStatusResponse::getPath)
        .returns(BACKEND, BackupCreateStatusResponse::getBackend)
        .returns(null, BackupCreateStatusResponse::getError)
        .extracting(BackupCreateStatusResponse::getStatus).isIn(CreateStatus.STARTED, CreateStatus.TRANSFERRING,
          CreateStatus.TRANSFERRED, CreateStatus.SUCCESS);

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

    assertThat(delete.hasErrors()).isFalse();
    assertThat(delete.getResult()).isTrue();

    // Start restoring backup
    Result<BackupRestoreResponse> restoreResult = client.backup().restorer()
      .backend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(restoreResult.hasErrors()).isFalse();
    assertThat(restoreResult.getResult()).isNotNull()
      .returns(backupId, BackupRestoreResponse::getId)
      .returns(new String[]{CLASS_NAME_PIZZA}, BackupRestoreResponse::getClassNames)
      // TODO remove/leave /snapshot.json
      // FIXME backups/filesystem/backup-1109550233/restore
//      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId + "/snapshot.json", BackupRestoreResponse::getPath)
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

      assertThat(restoreStatusResult.hasErrors()).isFalse();
      assertThat(restoreStatusResult.getResult()).isNotNull()
        .returns(backupId, BackupRestoreStatusResponse::getId)
        // TODO remove/leave /snapshot.json
        .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId + "/snapshot.json", BackupRestoreStatusResponse::getPath)
        .returns(BACKEND, BackupRestoreStatusResponse::getBackend)
        .returns(null, BackupRestoreStatusResponse::getError)
        .extracting(BackupRestoreStatusResponse::getStatus).isIn(RestoreStatus.STARTED, RestoreStatus.TRANSFERRING,
          RestoreStatus.TRANSFERRED, RestoreStatus.SUCCESS);

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
      // TODO remove/leave /snapshot.json
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId + "/snapshot.json", BackupCreateResponse::getPath)
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
      // TODO remove/leave /snapshot.json
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId + "/snapshot.json", BackupCreateStatusResponse::getPath)
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
      .backend(BACKEND)
      .withBackupId(backupId)
      .withWaitForCompletion(true)
      .run();

    assertThat(restoreResult.hasErrors()).isFalse();
    assertThat(restoreResult.getResult()).isNotNull()
      .returns(backupId, BackupRestoreResponse::getId)
      .returns(new String[]{CLASS_NAME_PIZZA}, BackupRestoreResponse::getClassNames)
      // TODO remove/leave /snapshot.json
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId + "/snapshot.json", BackupRestoreResponse::getPath)
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
      // TODO remove/leave /snapshot.json
      .returns(DOCKER_COMPOSE_BACKUPS_DIR + "/" + backupId + "/snapshot.json", BackupRestoreStatusResponse::getPath)
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
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
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
      // FIXME should be 422
      .returns(500, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(NOT_EXISTING_BACKEND);
  }

  @Test
  public void shouldFailOnRestoreBackupFromNotExistingBackend() {
    Result<BackupRestoreResponse> restoreResult = client.backup().restorer()
      .withIncludeClassNames(NOT_EXISTING_CLASS_NAME)
      .backend(NOT_EXISTING_BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(restoreResult.hasErrors()).isTrue();
    assertThat(restoreResult.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
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
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
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
      .backend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(restoreResult.hasErrors()).isTrue();
    assertThat(restoreResult.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(CLASS_NAME_PIZZA);
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
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
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
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains(notExistingBackupId);
  }

  @Test
  public void shouldFailOnRestoreOfNotExistingBackup() {
    Result<BackupRestoreResponse> restoreResult = client.backup().restorer()
      .withIncludeClassNames(NOT_EXISTING_CLASS_NAME)
      .backend(BACKEND)
      .withBackupId(notExistingBackupId)
      .run();

    assertThat(restoreResult.hasErrors()).isTrue();
    assertThat(restoreResult.getError()).isNotNull()
      .returns(404, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
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
      // FIXME should be 404?
      .returns(500, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      // TODO adjust to error message
      .first().asInstanceOf(CHAR_SEQUENCE).contains(BACKEND).contains(backupId);
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
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
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
      .backend(BACKEND)
      .withBackupId(backupId)
      .run();

    assertThat(restoreResult.hasErrors()).isTrue();
    assertThat(restoreResult.getError()).isNotNull()
      .returns(422, WeaviateError::getStatusCode)
      .extracting(WeaviateError::getMessages).asList()
      .hasSizeGreaterThan(0)
      .extracting(msg -> ((WeaviateErrorMessage)msg).getMessage())
      .first().asInstanceOf(CHAR_SEQUENCE).contains("include").contains("exclude");
  }

//  @Test
//  public void shouldGetAllExistingBackups() {
//    String backupIdPizza = backupId + "-pizza";
//    String backupIdSoup = backupId + "-soup";
//
//    Result<BackupCreateResponse> createResultPizza = client.backup().creator()
//      .withIncludeClassNames(CLASS_NAME_PIZZA)
//      .withBackend(BACKEND)
//      .withBackupId(backupIdPizza)
//      .withWaitForCompletion(true)
//      .run();
//
//    assertThat(createResultPizza.hasErrors()).isFalse();
//
//    Result<BackupCreateResponse> createResultSoup = client.backup().creator()
//      .withIncludeClassNames(CLASS_NAME_SOUP)
//      .withBackend(BACKEND)
//      .withBackupId(backupIdSoup)
//      .withWaitForCompletion(true)
//      .run();
//
//    assertThat(createResultSoup.hasErrors()).isFalse();
//
//    Result<BackupCreateResponse[]> allResult = client.backup().getter()
//      .withBackend(BACKEND)
//      .run();
//
//    assertThat(allResult.hasErrors()).isFalse();
//    assertThat(allResult.getResult()).isNotNull()
//      .hasSize(2)
//      .extracting(BackupCreateResponse::getId)
//      .containsExactlyInAnyOrder(backupIdPizza, backupIdSoup);
//  }

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
