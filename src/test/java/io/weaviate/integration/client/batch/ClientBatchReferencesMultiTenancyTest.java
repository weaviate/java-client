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
import io.weaviate.client.v1.schema.model.Tenant;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientBatchReferencesMultiTenancyTest {

  private WeaviateClient client;
  private WeaviateTestGenerics testGenerics;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    String httpHost = compose.getHttpHostAddress();
    Config config = new Config("http", httpHost);

    client = new WeaviateClient(config);
    testGenerics = new WeaviateTestGenerics();
  }

  @After
  public void after() {
    testGenerics.cleanupWeaviate(client);
  }

  @Test
  public void shouldCreateReferencesBetweenMTClasses() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenantNames);
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);
    testGenerics.createDataPizzaForTenants(client, tenantNames);

    createSoupToPizzaRefProp();

    BatchReference[] references = Arrays.stream(tenants).flatMap(tenant ->
      soupIds.stream().flatMap(soupId ->
        pizzaIds.stream().map(pizzaId ->
          client.batch().referencePayloadBuilder()
            .withTenant(tenant.getName())
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
          .withTenant(tenant.getName())
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
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenantNames);
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);
    testGenerics.createDataPizzaForTenants(client, tenantNames);

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
          .withTenant(tenant.getName())
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
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenantNames);
    testGenerics.createSchemaPizza(client);
    testGenerics.createDataPizza(client);

    createSoupToPizzaRefProp();

    BatchReference[] references = Arrays.stream(tenants).flatMap(tenant ->
      soupIds.stream().flatMap(soupId ->
        pizzaIds.stream().map(pizzaId ->
          client.batch().referencePayloadBuilder()
            .withTenant(tenant.getName())
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
          .withTenant(tenant.getName())
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
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenantNames);
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
          .withTenant(tenant.getName())
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
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    testGenerics.createSchemaSoup(client);
    testGenerics.createDataSoup(client);
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);
    testGenerics.createDataPizzaForTenants(client, tenantNames);

    createSoupToPizzaRefProp();

    BatchReference[] references = Arrays.stream(tenants).flatMap(tenant ->
      soupIds.stream().flatMap(soupId ->
        pizzaIds.stream().map(pizzaId ->
          client.batch().referencePayloadBuilder()
            .withTenant(tenant.getName())
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
