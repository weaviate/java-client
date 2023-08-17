package io.weaviate.integration.client.batch;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.BatchDeleteResponse;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.batch.model.ObjectGetResponseStatus;
import io.weaviate.client.v1.batch.model.ObjectsGetResponseAO2Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;

public class ClientBatchMultiTenancyTest {

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
  public void shouldCreateObjects() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);
    testGenerics.createTenantsSoup(client, tenants);

    Map<String, Object> propsQuatroFormaggi = new HashMap<>();
    propsQuatroFormaggi.put("name", "Quattro Formaggi");
    propsQuatroFormaggi.put("description", "Pizza quattro formaggi Italian: [ˈkwattro forˈmaddʒi] (four cheese pizza) is a variety of pizza in Italian cuisine that is topped with a combination of four kinds of cheese, usually melted together, with (rossa, red) or without (bianca, white) tomato sauce. It is popular worldwide, including in Italy,[1] and is one of the iconic items from pizzerias's menus.");
    propsQuatroFormaggi.put("price", 1.4f);
    propsQuatroFormaggi.put("bestBefore", "2022-01-02T03:04:05+01:00");

    Map<String, Object> propsFruttiDiMare = new HashMap<>();
    propsFruttiDiMare.put("name", "Frutti di Mare");
    propsFruttiDiMare.put("description", "Frutti di Mare is an Italian type of pizza that may be served with scampi, mussels or squid. It typically lacks cheese, with the seafood being served atop a tomato sauce.");
    propsFruttiDiMare.put("price", 2.5f);
    propsFruttiDiMare.put("bestBefore", "2022-02-03T04:05:06+02:00");

    Map<String, Object> propsChicken = new HashMap<>();
    propsChicken.put("name", "ChickenSoup");
    propsChicken.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
    propsChicken.put("price", 2f);
    propsChicken.put("bestBefore", "2022-05-06T07:08:09+05:00");

    Map<String, Object> propsBeautiful = new HashMap<>();
    propsBeautiful.put("name", "Beautiful");
    propsBeautiful.put("description", "Putting the game of letter soups to a whole new level.");
    propsBeautiful.put("price", 3f);
    propsBeautiful.put("bestBefore", "2022-06-07T08:09:10+06:00");

    Map<String, String> ids = new HashMap<>();
    ids.put(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID, "Pizza");
    ids.put(WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID, "Pizza");
    ids.put(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID, "Soup");
    ids.put(WeaviateTestGenerics.SOUP_BEAUTIFUL_ID, "Soup");

    WeaviateObject[] objects = Arrays.stream(tenants).flatMap(tenant -> {
      WeaviateObject pizzaQuatroFormaggi = WeaviateObject.builder()
        .id(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID)
        .className("Pizza")
        .properties(propsQuatroFormaggi)
        .tenant(tenant.getName())
        .build();

      WeaviateObject pizzaFruttiDiMare = WeaviateObject.builder()
        .id(WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID)
        .className("Pizza")
        .properties(propsFruttiDiMare)
        .tenant(tenant.getName())
        .build();

      WeaviateObject soupChicken = WeaviateObject.builder()
        .id(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
        .className("Soup")
        .properties(propsChicken)
        .tenant(tenant.getName())
        .build();

      WeaviateObject soupBeautiful = WeaviateObject.builder()
        .id(WeaviateTestGenerics.SOUP_BEAUTIFUL_ID)
        .className("Soup")
        .properties(propsBeautiful)
        .tenant(tenant.getName())
        .build();

      return Stream.of(pizzaQuatroFormaggi, pizzaFruttiDiMare, soupChicken, soupBeautiful);
    }).toArray(WeaviateObject[]::new);

    Result<ObjectGetResponse[]> result = client.batch().objectsBatcher()
      .withObjects(objects)
      .run();

    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asInstanceOf(ARRAY)
      .hasSize(4 * tenants.length);

    Map<String, List<ObjectGetResponse>> grouped = Arrays.stream(result.getResult())
      .collect(Collectors.groupingBy(ObjectGetResponse::getTenant));
    Arrays.stream(tenants).forEach(tenant -> {
      assertThat(grouped.get(tenant.getName())).isNotNull()
        .hasSize(4)
        .extracting(ObjectGetResponse::getId)
        .containsExactlyInAnyOrderElementsOf(ids.keySet());

      grouped.get(tenant.getName()).forEach(item ->
        assertThat(item).isNotNull()
          .returns(tenant.getName(), ObjectGetResponse::getTenant)
          .extracting(ObjectGetResponse::getResult)
          .returns(ObjectGetResponseStatus.SUCCESS, ObjectsGetResponseAO2Result::getStatus)
      );
    });

    // verify created
    Arrays.stream(tenants).forEach(tenant ->
      ids.forEach((id, className) ->
        assertMT.objectExists(className, id, tenant.getName())
      )
    );
  }

  @Test
  public void shouldNotCreateObjectsWithoutTenant() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);
    testGenerics.createTenantsSoup(client, tenants);

    Map<String, Object> propsQuatroFormaggi = new HashMap<>();
    propsQuatroFormaggi.put("name", "Quattro Formaggi");
    propsQuatroFormaggi.put("description", "Pizza quattro formaggi Italian: [ˈkwattro forˈmaddʒi] (four cheese pizza) is a variety of pizza in Italian cuisine that is topped with a combination of four kinds of cheese, usually melted together, with (rossa, red) or without (bianca, white) tomato sauce. It is popular worldwide, including in Italy,[1] and is one of the iconic items from pizzerias's menus.");
    propsQuatroFormaggi.put("price", 1.4f);
    propsQuatroFormaggi.put("bestBefore", "2022-01-02T03:04:05+01:00");

    Map<String, Object> propsFruttiDiMare = new HashMap<>();
    propsFruttiDiMare.put("name", "Frutti di Mare");
    propsFruttiDiMare.put("description", "Frutti di Mare is an Italian type of pizza that may be served with scampi, mussels or squid. It typically lacks cheese, with the seafood being served atop a tomato sauce.");
    propsFruttiDiMare.put("price", 2.5f);
    propsFruttiDiMare.put("bestBefore", "2022-02-03T04:05:06+02:00");

    Map<String, Object> propsChicken = new HashMap<>();
    propsChicken.put("name", "ChickenSoup");
    propsChicken.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
    propsChicken.put("price", 2f);
    propsChicken.put("bestBefore", "2022-05-06T07:08:09+05:00");

    Map<String, Object> propsBeautiful = new HashMap<>();
    propsBeautiful.put("name", "Beautiful");
    propsBeautiful.put("description", "Putting the game of letter soups to a whole new level.");
    propsBeautiful.put("price", 3f);
    propsBeautiful.put("bestBefore", "2022-06-07T08:09:10+06:00");

    Map<String, String> ids = new HashMap<>();
    ids.put(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID, "Pizza");
    ids.put(WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID, "Pizza");
    ids.put(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID, "Soup");
    ids.put(WeaviateTestGenerics.SOUP_BEAUTIFUL_ID, "Soup");

    WeaviateObject pizzaQuatroFormaggi = WeaviateObject.builder()
      .id(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID)
      .className("Pizza")
      .properties(propsQuatroFormaggi)
      .build();

    WeaviateObject pizzaFruttiDiMare = WeaviateObject.builder()
      .id(WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID)
      .className("Pizza")
      .properties(propsFruttiDiMare)
      .build();

    WeaviateObject soupChicken = WeaviateObject.builder()
      .id(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
      .className("Soup")
      .properties(propsChicken)
      .build();

    WeaviateObject soupBeautiful = WeaviateObject.builder()
      .id(WeaviateTestGenerics.SOUP_BEAUTIFUL_ID)
      .className("Soup")
      .properties(propsBeautiful)
      .build();

    Result<ObjectGetResponse[]> result = client.batch().objectsBatcher()
      .withObjects(pizzaQuatroFormaggi, pizzaFruttiDiMare, soupChicken, soupBeautiful)
      .run();

    assertThat(result).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asInstanceOf(ARRAY)
      .hasSize(4)
      .extracting(o -> ((ObjectGetResponse) o).getId())
      .containsExactlyInAnyOrderElementsOf(ids.keySet());

    Arrays.stream(result.getResult()).forEach(ogr ->
      assertThat(ogr).isNotNull()
        .returns(null, ObjectGetResponse::getTenant)
        .extracting(ObjectGetResponse::getResult)
        .returns(ObjectGetResponseStatus.FAILED, ObjectsGetResponseAO2Result::getStatus)
    );

    // verify not created
    Arrays.stream(tenants).forEach(tenant ->
      ids.forEach((id, className) ->
        assertMT.objectDoesNotExist(className, id, tenant.getName())
      )
    );
  }

  @Test
  public void shouldDeleteObjects() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenantNames);

    Arrays.stream(tenants).forEach(tenant ->
      WeaviateTestGenerics.IDS_BY_CLASS.forEach((className, ids) -> {
        Result<BatchDeleteResponse> result = client.batch().objectsBatchDeleter()
          .withClassName(className)
          .withTenant(tenant.getName())
          .withWhere(WhereFilter.builder()
            .operator(Operator.Like)
            .path(new String[]{"_id"})
            .valueText("*")
            .build())
          .run();

        assertThat(result).isNotNull()
          .returns(false, Result::hasErrors)
          .extracting(Result::getResult).isNotNull()
          .extracting(BatchDeleteResponse::getResults)
          .returns((long) ids.size(), BatchDeleteResponse.Results::getMatches)
          .returns((long) ids.size(), BatchDeleteResponse.Results::getSuccessful);

        // verify deleted
        ids.forEach(id -> assertMT.objectDoesNotExist(className, id, tenant.getName()));
      })
    );
  }

  @Test
  public void shouldNotDeleteObjectsWithoutTenant() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenantNames);

    WeaviateTestGenerics.IDS_BY_CLASS.forEach((className, ids) -> {
      Result<BatchDeleteResponse> result = client.batch().objectsBatchDeleter()
        .withClassName(className)
        .withWhere(WhereFilter.builder()
          .operator(Operator.Like)
          .path(new String[]{"_id"})
          .valueText("*")
          .build())
        .run();

      assertMT.error(result, null, 422, "has multi-tenancy enabled, but request was without tenant");
    });

    Arrays.stream(tenants).forEach(tenant ->
      WeaviateTestGenerics.IDS_BY_CLASS.forEach((className, ids) ->
        ids.forEach(id -> assertMT.objectExists(className, id, tenant.getName()))
      )
    );
  }
}
