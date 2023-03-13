package io.weaviate.integration.client.batch;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.BatchDeleteOutput;
import io.weaviate.client.v1.batch.model.BatchDeleteResponse;
import io.weaviate.client.v1.batch.model.BatchDeleteResultStatus;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.integration.client.WeaviateTestGenerics;

import java.io.File;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientBatchDeleteTest {

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

    client = new WeaviateClient(config);
    testGenerics.createTestSchemaAndData(client);
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void testBatchDeleteDryRunVerbose() {
    // when
    WhereFilter whereFilter = WhereFilter.builder()
            .operator(Operator.Equal)
            .path(new String[]{ "name" })
            .valueString("Hawaii")
            .build();

    int allWeaviateObjects = countWeaviateObjects();
    Result<BatchDeleteResponse> resResponse = client.batch().objectsBatchDeleter()
            .withDryRun(true)
            .withOutput(BatchDeleteOutput.VERBOSE)
            .withClassName("Pizza")
            .withWhere(whereFilter)
            .run();
    int remainingWeaviateObjects = countWeaviateObjects();

    // then
    assertThat(remainingWeaviateObjects).isEqualTo(allWeaviateObjects);
    assertThat(resResponse).isNotNull();
    assertThat(resResponse.hasErrors()).isFalse();

    BatchDeleteResponse response = resResponse.getResult();
    assertThat(response).isNotNull();
    assertThat(response.getDryRun()).isTrue();
    assertThat(response.getOutput()).isEqualTo(BatchDeleteOutput.VERBOSE);

    BatchDeleteResponse.Match match = response.getMatch();
    assertThat(match).isNotNull();
    assertThat(match.getClassName()).isEqualTo("Pizza");
    assertThat(match.getWhereFilter()).isEqualTo(whereFilter);

    BatchDeleteResponse.Results results = response.getResults();
    assertThat(results).isNotNull();
    assertThat(results.getSuccessful()).isZero();
    assertThat(results.getFailed()).isZero();
    assertThat(results.getLimit()).isEqualTo(10000L);
    assertThat(results.getMatches()).isEqualTo(1L);
    assertThat(results.getObjects()).hasSize(1);

    BatchDeleteResponse.ResultObject object = results.getObjects()[0];
    assertThat(object).isNotNull();
    assertThat(object.getId()).isEqualTo(WeaviateTestGenerics.PIZZA_HAWAII_ID);
    assertThat(object.getStatus()).isEqualTo(BatchDeleteResultStatus.DRYRUN);
    assertThat(object.getErrors()).isNull();
  }

  @Test
  public void testBatchDeleteDryRunMinimal() {
    // when
    WhereFilter whereFilter = WhereFilter.builder()
            .operator(Operator.Like)
            .path(new String[]{ "description" })
            .valueText("microscopic")
            .build();

    int allWeaviateObjects = countWeaviateObjects();
    Result<BatchDeleteResponse> resResponse = client.batch().objectsBatchDeleter()
            .withDryRun(true)
            .withOutput(BatchDeleteOutput.MINIMAL)
            .withClassName("Soup")
            .withWhere(whereFilter)
            .run();
    int remainingWeaviateObjects = countWeaviateObjects();

    // then
    assertThat(remainingWeaviateObjects).isEqualTo(allWeaviateObjects);
    assertThat(resResponse).isNotNull();
    assertThat(resResponse.hasErrors()).isFalse();

    BatchDeleteResponse response = resResponse.getResult();
    assertThat(response).isNotNull();
    assertThat(response.getDryRun()).isTrue();
    assertThat(response.getOutput()).isEqualTo(BatchDeleteOutput.MINIMAL);

    BatchDeleteResponse.Match match = response.getMatch();
    assertThat(match).isNotNull();
    assertThat(match.getClassName()).isEqualTo("Soup");
    assertThat(match.getWhereFilter()).isEqualTo(whereFilter);

    BatchDeleteResponse.Results results = response.getResults();
    assertThat(results).isNotNull();
    assertThat(results.getSuccessful()).isZero();
    assertThat(results.getFailed()).isZero();
    assertThat(results.getLimit()).isEqualTo(10000L);
    assertThat(results.getMatches()).isEqualTo(1L);
    assertThat(results.getObjects()).isNull();
  }

  @Test
  public void testBatchDeleteNoMatchWithDefaultOutputAndDryRun() {
    // when
    long inAMinute = Instant.now().plusSeconds(60).toEpochMilli();
    WhereFilter whereFilter = WhereFilter.builder()
            .operator(Operator.GreaterThan)
            .path(new String[]{ "_creationTimeUnix" })
            .valueString(Long.toString(inAMinute))
            .build();

    int allWeaviateObjects = countWeaviateObjects();
    Result<BatchDeleteResponse> response = client.batch().objectsBatchDeleter()
            .withClassName("Pizza")
            .withWhere(whereFilter)
            .withConsistencyLevel(ConsistencyLevel.QUORUM)
            .run();
    int remainingWeaviateObjects = countWeaviateObjects();

    // then
    assertThat(remainingWeaviateObjects).isEqualTo(allWeaviateObjects);
    assertThat(response).isNotNull();
    assertThat(response.hasErrors()).isFalse();

    BatchDeleteResponse result = response.getResult();
    assertThat(response.getResult()).isNotNull();
    assertThat(result.getDryRun()).isFalse();
    assertThat(result.getOutput()).isEqualTo(BatchDeleteOutput.MINIMAL);

    BatchDeleteResponse.Match match = result.getMatch();
    assertThat(match).isNotNull();
    assertThat(match.getClassName()).isEqualTo("Pizza");
    assertThat(match.getWhereFilter()).isEqualTo(whereFilter);

    BatchDeleteResponse.Results results = result.getResults();
    assertThat(results).isNotNull();
    assertThat(results.getSuccessful()).isZero();
    assertThat(results.getFailed()).isZero();
    assertThat(results.getLimit()).isEqualTo(10000L);
    assertThat(results.getMatches()).isZero();
    assertThat(results.getObjects()).isNull();
  }

  @Test
  public void testBatchDeleteAllMatchesWithDefaultDryRun() {
    // when
    long inAMinute = Instant.now().plusSeconds(60).toEpochMilli();
    WhereFilter whereFilter = WhereFilter.builder()
            .operator(Operator.LessThan)
            .path(new String[]{ "_creationTimeUnix" })
            .valueString(Long.toString(inAMinute))
            .build();

    int allWeaviateObjects = countWeaviateObjects();
    Result<BatchDeleteResponse> response = client.batch().objectsBatchDeleter()
            .withOutput(BatchDeleteOutput.VERBOSE)
            .withClassName("Pizza")
            .withWhere(whereFilter)
            .withConsistencyLevel(ConsistencyLevel.QUORUM)
            .run();
    int remainingWeaviateObjects = countWeaviateObjects();

    // then
    assertThat(remainingWeaviateObjects).isEqualTo(allWeaviateObjects - 4);
    assertThat(response).isNotNull();
    assertThat(response.hasErrors()).isFalse();

    BatchDeleteResponse result = response.getResult();
    assertThat(response.getResult()).isNotNull();
    assertThat(result.getDryRun()).isFalse();
    assertThat(result.getOutput()).isEqualTo(BatchDeleteOutput.VERBOSE);

    BatchDeleteResponse.Match match = result.getMatch();
    assertThat(match).isNotNull();
    assertThat(match.getClassName()).isEqualTo("Pizza");
    assertThat(match.getWhereFilter()).isEqualTo(whereFilter);

    BatchDeleteResponse.Results results = result.getResults();
    assertThat(results).isNotNull();
    assertThat(results.getSuccessful()).isEqualTo(4);
    assertThat(results.getFailed()).isZero();
    assertThat(results.getLimit()).isEqualTo(10000L);
    assertThat(results.getMatches()).isEqualTo(4);

    BatchDeleteResponse.ResultObject[] objects = results.getObjects();
    assertThat(objects).hasSize(4);
    assertThat(objects).doesNotContainNull();
    assertThat(objects).extracting(BatchDeleteResponse.ResultObject::getStatus)
            .containsOnly(BatchDeleteResultStatus.SUCCESS);
    assertThat(objects).extracting(BatchDeleteResponse.ResultObject::getErrors)
            .containsOnlyNulls();
    assertThat(objects).extracting(BatchDeleteResponse.ResultObject::getId)
            .contains(WeaviateTestGenerics.PIZZA_HAWAII_ID, WeaviateTestGenerics.PIZZA_DOENER_ID,
                    WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID, WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID);
  }

  private int countWeaviateObjects() {
    Result<List<WeaviateObject>> resResponse = client.data().objectsGetter().run();
    assertThat(resResponse).isNotNull();
    assertThat(resResponse.hasErrors()).isFalse();

    List<WeaviateObject> response = resResponse.getResult();
    assertThat(response).isNotNull();

    return response.size();
  }
}
