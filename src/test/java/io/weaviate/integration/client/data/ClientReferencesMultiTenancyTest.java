package io.weaviate.integration.client.data;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.BatchReference;
import io.weaviate.client.v1.batch.model.BatchReferenceResponse;
import io.weaviate.client.v1.batch.model.BatchReferenceResponseAO1Result;
import io.weaviate.client.v1.batch.model.BatchReferenceResponseStatus;
import io.weaviate.client.v1.data.model.SingleRef;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Tenant;
import io.weaviate.integration.client.AssertMultiTenancy;
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
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;

public class ClientReferencesMultiTenancyTest {

  private WeaviateClient client;
  private WeaviateTestGenerics testGenerics;
  private AssertMultiTenancy assertMT;

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
    assertMT = new AssertMultiTenancy(client);
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

    Arrays.stream(tenants).forEach(tenant ->
      soupIds.forEach(soupId ->
        pizzaIds.forEach(pizzaId -> {
          SingleRef pizzaRef = client.data().referencePayloadBuilder()
            .withClassName("Pizza")
            .withID(pizzaId)
            .payload();

          Result<Boolean> result = client.data().referenceCreator()
            .withClassName("Soup")
            .withID(soupId)
            .withReferenceProperty("relatedToPizza")
            .withReference(pizzaRef)
            .withTenant(tenant.getName())
            .run();

          assertThat(result).isNotNull()
            .returns(false, Result::hasErrors)
            .returns(true, Result::getResult);
        })
      )
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

    soupIds.forEach(soupId ->
      pizzaIds.forEach(pizzaId -> {
        SingleRef pizzaRef = client.data().referencePayloadBuilder()
          .withClassName("Pizza")
          .withID(pizzaId)
          .payload();

        Result<Boolean> refAddResult = client.data().referenceCreator()
          .withClassName("Soup")
          .withID(soupId)
          .withReferenceProperty("relatedToPizza")
          .withReference(pizzaRef)
          .run();

        assertMT.error(refAddResult, false, 422, "has multi-tenancy enabled, but request was without tenant");
      })
    );

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
  public void shouldDeleteReferencesBetweenMTClasses() {
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
    createSoupToPizzaRefs(soupIds, pizzaIds, tenantNames);

    Arrays.stream(tenants).forEach(tenant ->
      soupIds.forEach(soupId -> {
        int[] expectedRefsLeft = new int[]{pizzaIds.size()};

        pizzaIds.forEach(pizzaId -> {
          SingleRef pizzaRef = client.data().referencePayloadBuilder()
            .withClassName("Pizza")
            .withID(pizzaId)
            .payload();

          Result<Boolean> result = client.data().referenceDeleter()
            .withClassName("Soup")
            .withID(soupId)
            .withReferenceProperty("relatedToPizza")
            .withReference(pizzaRef)
            .withTenant(tenant.getName())
            .run();

          assertThat(result).isNotNull()
            .returns(false, Result::hasErrors)
            .returns(true, Result::getResult);

          // verify deleted one by one
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
            .hasSize(--expectedRefsLeft[0]);
        });
      })
    );
  }

  @Test
  public void shouldNotDeleteReferencesBetweenMTClassesWithoutTenant() {
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
    createSoupToPizzaRefs(soupIds, pizzaIds, tenantNames);

    soupIds.forEach(soupId -> {
      pizzaIds.forEach(pizzaId -> {
        SingleRef pizzaRef = client.data().referencePayloadBuilder()
          .withClassName("Pizza")
          .withID(pizzaId)
          .payload();

        Result<Boolean> result = client.data().referenceDeleter()
          .withClassName("Soup")
          .withID(soupId)
          .withReferenceProperty("relatedToPizza")
          .withReference(pizzaRef)
          .run();

        assertMT.error(result, false, 500, "has multi-tenancy enabled, but request was without tenant"); // TODO 422?
      });
    });

    // verify not deleted
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
  public void shouldReplaceReferencesBetweenMTClasses() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    List<String> pizzaIdsBefore = pizzaIds.subList(0, 2);
    List<String> pizzaIdsAfter = pizzaIds.subList(2, pizzaIds.size());
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenantNames);
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);
    testGenerics.createDataPizzaForTenants(client, tenantNames);

    createSoupToPizzaRefProp();
    createSoupToPizzaRefs(soupIds, pizzaIdsBefore, tenantNames);

    Arrays.stream(tenants).forEach(tenant ->
      soupIds.forEach(soupId -> {
        SingleRef[] refs = pizzaIdsAfter.stream().map(pizzaId ->
          client.data().referencePayloadBuilder()
            .withClassName("Pizza")
            .withID(pizzaId)
            .payload()
        ).toArray(SingleRef[]::new);

        Result<Boolean> result = client.data().referenceReplacer()
          .withClassName("Soup")
          .withID(soupId)
          .withReferenceProperty("relatedToPizza")
          .withReferences(refs)
          .withTenant(tenant.getName())
          .run();

        assertThat(result).isNotNull()
          .returns(false, Result::hasErrors)
          .returns(true, Result::getResult);
      })
    );

    // verify replaced
    Arrays.stream(tenants).forEach(tenant -> {
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
          .hasSize(pizzaIdsAfter.size());

        List<String> beacons = getSoupResult.getResult().stream()
          .map(WeaviateObject::getProperties)
          .map(p -> p.get("relatedToPizza"))
          .flatMap(refs -> ((List<Map<String, String>>) refs).stream())
          .map(ref -> ref.get("beacon"))
          .collect(Collectors.toList());

        pizzaIdsAfter.forEach(pizzaId ->
          assertThat(beacons.stream().anyMatch(beacon -> beacon.contains(pizzaId))).isTrue()
        );
      });
    });
  }

  @Test
  public void shouldNotReplaceReferencesBetweenMTClassesWithoutTenant() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    List<String> pizzaIdsBefore = pizzaIds.subList(0, 2);
    List<String> pizzaIdsAfter = pizzaIds.subList(2, pizzaIds.size());
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenantNames);
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);
    testGenerics.createDataPizzaForTenants(client, tenantNames);

    createSoupToPizzaRefProp();
    createSoupToPizzaRefs(soupIds, pizzaIdsBefore, tenantNames);

    soupIds.forEach(soupId -> {
      SingleRef[] refs = pizzaIdsAfter.stream().map(pizzaId ->
        client.data().referencePayloadBuilder()
          .withClassName("Pizza")
          .withID(pizzaId)
          .payload()
      ).toArray(SingleRef[]::new);

      Result<Boolean> result = client.data().referenceReplacer()
        .withClassName("Soup")
        .withID(soupId)
        .withReferenceProperty("relatedToPizza")
        .withReferences(refs)
        .run();

      assertMT.error(result, false, 500, "has multi-tenancy enabled, but request was without tenant"); // TODO 422?
    });

    // verify not replaced
    Arrays.stream(tenants).forEach(tenant -> {
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
          .hasSize(pizzaIdsBefore.size());

        List<String> beacons = getSoupResult.getResult().stream()
          .map(WeaviateObject::getProperties)
          .map(p -> p.get("relatedToPizza"))
          .flatMap(refs -> ((List<Map<String, String>>) refs).stream())
          .map(ref -> ref.get("beacon"))
          .collect(Collectors.toList());

        pizzaIdsBefore.forEach(pizzaId ->
          assertThat(beacons.stream().anyMatch(beacon -> beacon.contains(pizzaId))).isTrue()
        );
      });
    });
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

    Arrays.stream(tenants).forEach(tenant ->
      soupIds.forEach(soupId ->
        pizzaIds.forEach(pizzaId -> {
          SingleRef pizzaRef = client.data().referencePayloadBuilder()
            .withClassName("Pizza")
            .withID(pizzaId)
            .payload();

          Result<Boolean> result = client.data().referenceCreator()
            .withClassName("Soup")
            .withID(soupId)
            .withReferenceProperty("relatedToPizza")
            .withReference(pizzaRef)
            .withTenant(tenant.getName())
            .run();

          assertThat(result).isNotNull()
            .returns(false, Result::hasErrors)
            .returns(true, Result::getResult);
        })
      )
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
  public void shouldNotCreateReferencesBetweenMTAndNotMTClassesWithoutTenant() {
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

    soupIds.forEach(soupId ->
      pizzaIds.forEach(pizzaId -> {
        SingleRef pizzaRef = client.data().referencePayloadBuilder()
          .withClassName("Pizza")
          .withID(pizzaId)
          .payload();

        Result<Boolean> refAddResult = client.data().referenceCreator()
          .withClassName("Soup")
          .withID(soupId)
          .withReferenceProperty("relatedToPizza")
          .withReference(pizzaRef)
          .run();

        assertMT.error(refAddResult, false, 500, "has multi-tenancy enabled, but request was without tenant"); // TODO 422?
      })
    );

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
  public void shouldDeleteReferencesBetweenMTAndNonMTClasses() {
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
    createSoupToPizzaRefs(soupIds, pizzaIds, tenantNames);

    Arrays.stream(tenants).forEach(tenant ->
      soupIds.forEach(soupId -> {
        int[] expectedRefsLeft = new int[]{pizzaIds.size()};

        pizzaIds.forEach(pizzaId -> {
          SingleRef pizzaRef = client.data().referencePayloadBuilder()
            .withClassName("Pizza")
            .withID(pizzaId)
            .payload();

          Result<Boolean> result = client.data().referenceDeleter()
            .withClassName("Soup")
            .withID(soupId)
            .withReferenceProperty("relatedToPizza")
            .withReference(pizzaRef)
            .withTenant(tenant.getName())
            .run();

          assertThat(result).isNotNull()
            .returns(false, Result::hasErrors)
            .returns(true, Result::getResult);

          // verify deleted one by one
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
            .hasSize(--expectedRefsLeft[0]);
        });
      })
    );
  }

  @Test
  public void shouldNotDeleteReferencesBetweenMTAndNonMTClassesWithoutTenant() {
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
    createSoupToPizzaRefs(soupIds, pizzaIds, tenantNames);

    soupIds.forEach(soupId -> {
      pizzaIds.forEach(pizzaId -> {
        SingleRef pizzaRef = client.data().referencePayloadBuilder()
          .withClassName("Pizza")
          .withID(pizzaId)
          .payload();

        Result<Boolean> result = client.data().referenceDeleter()
          .withClassName("Soup")
          .withID(soupId)
          .withReferenceProperty("relatedToPizza")
          .withReference(pizzaRef)
          .run();

        assertMT.error(result, false, 500, "has multi-tenancy enabled, but request was without tenant"); // TODO 422?
      });
    });

    // verify not deleted
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
  public void shouldReplaceReferencesBetweenMTAndNonMTClasses() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    List<String> pizzaIdsBefore = pizzaIds.subList(0, 2);
    List<String> pizzaIdsAfter = pizzaIds.subList(2, pizzaIds.size());
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenantNames);
    testGenerics.createSchemaPizza(client);
    testGenerics.createDataPizza(client);

    createSoupToPizzaRefProp();
    createSoupToPizzaRefs(soupIds, pizzaIdsBefore, tenantNames);

    Arrays.stream(tenants).forEach(tenant ->
      soupIds.forEach(soupId -> {
        SingleRef[] refs = pizzaIdsAfter.stream().map(pizzaId ->
          client.data().referencePayloadBuilder()
            .withClassName("Pizza")
            .withID(pizzaId)
            .payload()
        ).toArray(SingleRef[]::new);

        Result<Boolean> result = client.data().referenceReplacer()
          .withClassName("Soup")
          .withID(soupId)
          .withReferenceProperty("relatedToPizza")
          .withReferences(refs)
          .withTenant(tenant.getName())
          .run();

        assertThat(result).isNotNull()
          .returns(false, Result::hasErrors)
          .returns(true, Result::getResult);
      })
    );

    // verify replaced
    Arrays.stream(tenants).forEach(tenant -> {
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
          .hasSize(pizzaIdsAfter.size());

        List<String> beacons = getSoupResult.getResult().stream()
          .map(WeaviateObject::getProperties)
          .map(p -> p.get("relatedToPizza"))
          .flatMap(refs -> ((List<Map<String, String>>) refs).stream())
          .map(ref -> ref.get("beacon"))
          .collect(Collectors.toList());

        pizzaIdsAfter.forEach(pizzaId ->
          assertThat(beacons.stream().anyMatch(beacon -> beacon.contains(pizzaId))).isTrue()
        );
      });
    });
  }

  @Test
  public void shouldNotReplaceReferencesBetweenMTAndNonMtClassesWithoutTenant() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    List<String> pizzaIdsBefore = pizzaIds.subList(0, 2);
    List<String> pizzaIdsAfter = pizzaIds.subList(2, pizzaIds.size());
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenantNames);
    testGenerics.createSchemaPizza(client);
    testGenerics.createDataPizza(client);

    createSoupToPizzaRefProp();
    createSoupToPizzaRefs(soupIds, pizzaIdsBefore, tenantNames);

    soupIds.forEach(soupId -> {
      SingleRef[] refs = pizzaIdsAfter.stream().map(pizzaId ->
        client.data().referencePayloadBuilder()
          .withClassName("Pizza")
          .withID(pizzaId)
          .payload()
      ).toArray(SingleRef[]::new);

      Result<Boolean> result = client.data().referenceReplacer()
        .withClassName("Soup")
        .withID(soupId)
        .withReferenceProperty("relatedToPizza")
        .withReferences(refs)
        .run();

      assertMT.error(result, false, 500, "has multi-tenancy enabled, but request was without tenant"); // TODO 422?
    });

    // verify not replaced
    Arrays.stream(tenants).forEach(tenant -> {
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
          .hasSize(pizzaIdsBefore.size());

        List<String> beacons = getSoupResult.getResult().stream()
          .map(WeaviateObject::getProperties)
          .map(p -> p.get("relatedToPizza"))
          .flatMap(refs -> ((List<Map<String, String>>) refs).stream())
          .map(ref -> ref.get("beacon"))
          .collect(Collectors.toList());

        pizzaIdsBefore.forEach(pizzaId ->
          assertThat(beacons.stream().anyMatch(beacon -> beacon.contains(pizzaId))).isTrue()
        );
      });
    });
  }

  @Test
  public void shouldNotCreateReferencesBetweenNonMTAndMTClasses() {
    Tenant tenantPizza = Tenant.builder().name("tenantPizza").build();
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    testGenerics.createSchemaSoup(client);
    testGenerics.createDataSoup(client);
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenantPizza);
    testGenerics.createDataPizzaForTenants(client, tenantPizza.getName());

    createSoupToPizzaRefProp();

    soupIds.forEach(soupId ->
      pizzaIds.forEach(pizzaId -> {
        SingleRef pizzaRef = client.data().referencePayloadBuilder()
          .withClassName("Pizza")
          .withID(pizzaId)
          .payload();

        Result<Boolean> result = client.data().referenceCreator()
          .withClassName("Soup")
          .withID(soupId)
          .withReferenceProperty("relatedToPizza")
          .withReference(pizzaRef)
          .withTenant(tenantPizza.getName())
          .run();

        assertMT.error(result, false, 500, "has multi-tenancy disabled, but request was with tenant"); // TODO 422?
      })
    );

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

  @Test
  public void shouldNotReplaceReferencesBetweenNonMTAndMTClasses() {
    Tenant tenantPizza = Tenant.builder().name("tenantPizza").build();
    List<String> soupIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Soup");
    List<String> pizzaIds = WeaviateTestGenerics.IDS_BY_CLASS.get("Pizza");
    testGenerics.createSchemaSoup(client);
    testGenerics.createDataSoup(client);
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenantPizza);
    testGenerics.createDataPizzaForTenants(client, tenantPizza.getName());

    createSoupToPizzaRefProp();

    soupIds.forEach(soupId -> {
      SingleRef[] refs = pizzaIds.stream().map(pizzaId ->
        client.data().referencePayloadBuilder()
          .withClassName("Pizza")
          .withID(pizzaId)
          .payload()
      ).toArray(SingleRef[]::new);

      Result<Boolean> result = client.data().referenceReplacer()
        .withClassName("Soup")
        .withID(soupId)
        .withReferenceProperty("relatedToPizza")
        .withReferences(refs)
        .withTenant(tenantPizza.getName())
        .run();

      assertMT.error(result, false, 500, "has multi-tenancy disabled, but request was with tenant"); // TODO 422?
    });

    // verify not replaced
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

  private void createSoupToPizzaRefs(List<String> soupIds, List<String> pizzaIds, String... tenants) {
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
  }
}
