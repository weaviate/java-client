package io.weaviate.integration.client.batch;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.BatchReference;
import io.weaviate.client.v1.batch.model.BatchReferenceResponse;
import io.weaviate.client.v1.batch.model.BatchReferenceResponseAO1Result;
import io.weaviate.client.v1.batch.model.BatchReferenceResponseStatus;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.integration.client.WeaviateTestGenerics;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;

public class ClientBatchReferencesMultiTenancyTest {

  private WeaviateClient client;
  private WeaviateTestGenerics testGenerics;

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
    testGenerics = new WeaviateTestGenerics();
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void shouldCreateReferencesBetweenMTClasses() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2"};
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenants);
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);
    testGenerics.createDataPizzaForTenants(client, tenants);

    createSoupToPizzaRefProp();

    BatchReference[] references = Arrays.stream(tenants).flatMap(tenant ->
      soupIds.stream().flatMap(soupId ->
        pizzaIds.stream().map(pizzaId ->
          client.batch().referencePayloadBuilder()
            .withTenant(tenant)
            .withFromClassName("Soup")
            .withFromID(soupId)
            .withFromRefProp("relatedToPizza")
            .withToClassName("Pizza")
            .withToID(pizzaId)
            .payload()
        )
      )
    ).toArray(BatchReference[]::new);

    Result<BatchReferenceResponse[]> result = client.batch().referencesBatcher()
      .withReferences(references)
      .run();

    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asInstanceOf(ARRAY)
      .hasSize(tenants.length * pizzaIds.size() * soupIds.size());

    Arrays.stream(result.getResult()).forEach(item ->
      assertThat(item).isNotNull()
        .extracting(BatchReferenceResponse::getResult)
        .isNotNull()
        .returns(BatchReferenceResponseStatus.SUCCESS, BatchReferenceResponseAO1Result::getStatus)
    );

    // verify created
    Arrays.stream(tenants).forEach(tenant ->
      soupIds.forEach(soupId -> {
        Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
          .withTenant(tenant)
          .withClassName("Soup")
          .withID(soupId)
          .run();

        assertThat(getSoupResult).isNotNull()
          .returns(false, Result::hasErrors)
          .extracting(Result::getResult).asList()
          .hasSize(1)
          .first()
          .extracting(o -> ((WeaviateObject) o).getProperties())
          .extracting(p -> p.get("relatedToPizza")).asList()
          .hasSize(pizzaIds.size());
      })
    );
  }

  @Test
  public void shouldNotCreateReferencesBetweenMTClassesWithoutTenant() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2"};
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenants);
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);
    testGenerics.createDataPizzaForTenants(client, tenants);

    createSoupToPizzaRefProp();

    BatchReference[] references = soupIds.stream().flatMap(soupId ->
      pizzaIds.stream().map(pizzaId ->
        client.batch().referencePayloadBuilder()
          .withFromClassName("Soup")
          .withFromID(soupId)
          .withFromRefProp("relatedToPizza")
          .withToClassName("Pizza")
          .withToID(pizzaId)
          .payload()
      )
    ).toArray(BatchReference[]::new);

    Result<BatchReferenceResponse[]> result = client.batch().referencesBatcher()
      .withReferences(references)
      .run();

    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asInstanceOf(ARRAY)
      .hasSize(pizzaIds.size() * soupIds.size());

    Arrays.stream(result.getResult()).forEach(item -> {
      assertThat(item).isNotNull()
        .extracting(BatchReferenceResponse::getResult)
        .isNotNull()
        .returns(BatchReferenceResponseStatus.FAILED, BatchReferenceResponseAO1Result::getStatus)
        .extracting(BatchReferenceResponseAO1Result::getErrors)
        .extracting(BatchReferenceResponseAO1Result.ErrorResponse::getError).asList()
        .first()
        .extracting(i -> ((BatchReferenceResponseAO1Result.ErrorItem) i).getMessage()).asString()
        .contains("has multi-tenancy enabled, but request was without tenant");
    });

    // verify not created
    Arrays.stream(tenants).forEach(tenant ->
      soupIds.forEach(soupId -> {
        Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
          .withTenant(tenant)
          .withClassName("Soup")
          .withID(soupId)
          .run();

        assertThat(getSoupResult).isNotNull()
          .returns(false, Result::hasErrors)
          .extracting(Result::getResult).asList()
          .hasSize(1)
          .first()
          .extracting(o -> ((WeaviateObject) o).getProperties())
          .extracting(p -> p.get("relatedToPizza"))
          .isNull();
      })
    );
  }

  @Test
  public void shouldCreateReferencesBetweenMTAndNonMTClasses() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2"};
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenants);
    testGenerics.createSchemaPizza(client);
    testGenerics.createDataPizza(client);

    createSoupToPizzaRefProp();

    BatchReference[] references = Arrays.stream(tenants).flatMap(tenant ->
      soupIds.stream().flatMap(soupId ->
        pizzaIds.stream().map(pizzaId ->
          client.batch().referencePayloadBuilder()
            .withTenant(tenant)
            .withFromClassName("Soup")
            .withFromID(soupId)
            .withFromRefProp("relatedToPizza")
            .withToClassName("Pizza")
            .withToID(pizzaId)
            .payload()
        )
      )
    ).toArray(BatchReference[]::new);

    Result<BatchReferenceResponse[]> result = client.batch().referencesBatcher()
      .withReferences(references)
      .run();

    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asInstanceOf(ARRAY)
      .hasSize(tenants.length * pizzaIds.size() * soupIds.size());

    Arrays.stream(result.getResult()).forEach(item ->
      assertThat(item).isNotNull()
        .extracting(BatchReferenceResponse::getResult)
        .isNotNull()
        .returns(BatchReferenceResponseStatus.SUCCESS, BatchReferenceResponseAO1Result::getStatus)
    );

    // verify created
    Arrays.stream(tenants).forEach(tenant ->
      soupIds.forEach(soupId -> {
        Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
          .withTenant(tenant)
          .withClassName("Soup")
          .withID(soupId)
          .run();

        assertThat(getSoupResult).isNotNull()
          .returns(false, Result::hasErrors)
          .extracting(Result::getResult).asList()
          .hasSize(1)
          .first()
          .extracting(o -> ((WeaviateObject) o).getProperties())
          .extracting(p -> p.get("relatedToPizza")).asList()
          .hasSize(pizzaIds.size());
      })
    );
  }

  @Test
  public void shouldNotCreateReferencesBetweenMTAndNonMTClassesWithoutTenant() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2"};
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenants);
    testGenerics.createSchemaPizza(client);
    testGenerics.createDataPizza(client);

    createSoupToPizzaRefProp();

    BatchReference[] references = soupIds.stream().flatMap(soupId ->
      pizzaIds.stream().map(pizzaId ->
        client.batch().referencePayloadBuilder()
          .withFromClassName("Soup")
          .withFromID(soupId)
          .withFromRefProp("relatedToPizza")
          .withToClassName("Pizza")
          .withToID(pizzaId)
          .payload()
      )
    ).toArray(BatchReference[]::new);

    Result<BatchReferenceResponse[]> result = client.batch().referencesBatcher()
      .withReferences(references)
      .run();

    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asInstanceOf(ARRAY)
      .hasSize(pizzaIds.size() * soupIds.size());

    Arrays.stream(result.getResult()).forEach(item -> {
      assertThat(item).isNotNull()
        .extracting(BatchReferenceResponse::getResult)
        .isNotNull()
        .returns(BatchReferenceResponseStatus.FAILED, BatchReferenceResponseAO1Result::getStatus)
        .extracting(BatchReferenceResponseAO1Result::getErrors)
        .extracting(BatchReferenceResponseAO1Result.ErrorResponse::getError).asList()
        .first()
        .extracting(i -> ((BatchReferenceResponseAO1Result.ErrorItem) i).getMessage()).asString()
        .contains("has multi-tenancy enabled, but request was without tenant");
    });

    // verify not created
    Arrays.stream(tenants).forEach(tenant ->
      soupIds.forEach(soupId -> {
        Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
          .withTenant(tenant)
          .withClassName("Soup")
          .withID(soupId)
          .run();

        assertThat(getSoupResult).isNotNull()
          .returns(false, Result::hasErrors)
          .extracting(Result::getResult).asList()
          .hasSize(1)
          .first()
          .extracting(o -> ((WeaviateObject) o).getProperties())
          .extracting(p -> p.get("relatedToPizza"))
          .isNull();
      })
    );
  }

  @Test
  public void shouldNotCreateReferencesBetweenNonMTAndMTClasses() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2"};
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    testGenerics.createSchemaSoup(client);
    testGenerics.createDataSoup(client);
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);
    testGenerics.createDataPizzaForTenants(client, tenants);

    createSoupToPizzaRefProp();

    BatchReference[] references = Arrays.stream(tenants).flatMap(tenant ->
      soupIds.stream().flatMap(soupId ->
        pizzaIds.stream().map(pizzaId ->
          client.batch().referencePayloadBuilder()
            .withTenant(tenant)
            .withFromClassName("Soup")
            .withFromID(soupId)
            .withFromRefProp("relatedToPizza")
            .withToClassName("Pizza")
            .withToID(pizzaId)
            .payload()
        )
      )
    ).toArray(BatchReference[]::new);

    Result<BatchReferenceResponse[]> result = client.batch().referencesBatcher()
      .withReferences(references)
      .run();

    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asInstanceOf(ARRAY)
      .hasSize(tenants.length * pizzaIds.size() * soupIds.size());

    Arrays.stream(result.getResult()).forEach(item -> {
      assertThat(item).isNotNull()
        .extracting(BatchReferenceResponse::getResult)
        .isNotNull()
        .returns(BatchReferenceResponseStatus.FAILED, BatchReferenceResponseAO1Result::getStatus)
        .extracting(BatchReferenceResponseAO1Result::getErrors)
        .extracting(BatchReferenceResponseAO1Result.ErrorResponse::getError).asList()
        .first()
        .extracting(i -> ((BatchReferenceResponseAO1Result.ErrorItem) i).getMessage()).asString()
        .contains("cannot reference a multi-tenant enabled class from a non multi-tenant enabled class");
    });

    // verify not created
    soupIds.forEach(soupId -> {
      Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
        .withClassName("Soup")
        .withID(soupId)
        .run();

      assertThat(getSoupResult).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList()
        .hasSize(1)
        .first()
        .extracting(o -> ((WeaviateObject) o).getProperties())
        .extracting(p -> p.get("relatedToPizza"))
        .isNull();
    });
  }


  private void createSoupToPizzaRefProp() {
    Result<Boolean> refPropResult = client.schema().propertyCreator()
      .withClassName("Soup")
      .withProperty(Property.builder()
        .name("relatedToPizza")
        .dataType(Collections.singletonList("Pizza"))
        .build())
      .run();

    assertThat(refPropResult).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
  }
}
