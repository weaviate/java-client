package io.weaviate.integration.client.data;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.Tenant;
import io.weaviate.integration.client.AssertMultiTenancy;
import io.weaviate.integration.client.WeaviateDockerCompose;
import io.weaviate.integration.client.WeaviateTestGenerics;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClientDataMultiTenancyTest {

  private WeaviateClient client;
  private WeaviateTestGenerics testGenerics;
  private AssertMultiTenancy assertMT;

  @ClassRule
  public static WeaviateDockerCompose compose = new WeaviateDockerCompose();

  @Before
  public void before() {
    String httpHost = compose.getHttpHostAddress();
    Config config = new Config("http", httpHost);

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
    testGenerics.createTenantsPizza(client, tenants);

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

    Arrays.stream(tenants).forEach(tenant -> {
      Result<WeaviateObject> pizzaQuatroFormaggiStatus = client.data().creator()
        .withClassName("Pizza")
        .withID(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID)
        .withProperties(propsQuatroFormaggi)
        .withTenant(tenant.getName())
        .run();

      assertThat(pizzaQuatroFormaggiStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult)
        .returns(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID, WeaviateObject::getId)
        .returns("Pizza", WeaviateObject::getClassName)
        .returns(tenant.getName(), WeaviateObject::getTenant)
        .extracting(WeaviateObject::getProperties)
        .returns("Quattro Formaggi", p -> p.get("name"))
        .returns(1.4d, p -> p.get("price"))
        .returns("Pizza quattro formaggi Italian: [ˈkwattro forˈmaddʒi] (four cheese pizza) is a variety of pizza in Italian cuisine that is topped with a combination of four kinds of cheese, usually melted together, with (rossa, red) or without (bianca, white) tomato sauce. It is popular worldwide, including in Italy,[1] and is one of the iconic items from pizzerias's menus.", p -> p.get("description"))
        .returns("2022-01-02T03:04:05+01:00", p -> p.get("bestBefore"));

      Result<WeaviateObject> pizzaFruttiDiMareStatus = client.data().creator()
        .withClassName("Pizza")
        .withID(WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID)
        .withProperties(propsFruttiDiMare)
        .withTenant(tenant.getName())
        .run();

      assertThat(pizzaFruttiDiMareStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult)
        .returns(WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID, WeaviateObject::getId)
        .returns("Pizza", WeaviateObject::getClassName)
        .returns(tenant.getName(), WeaviateObject::getTenant)
        .extracting(WeaviateObject::getProperties)
        .returns("Frutti di Mare", p -> p.get("name"))
        .returns(2.5d, p -> p.get("price"))
        .returns("Frutti di Mare is an Italian type of pizza that may be served with scampi, mussels or squid. It typically lacks cheese, with the seafood being served atop a tomato sauce.", p -> p.get("description"))
        .returns("2022-02-03T04:05:06+02:00", p -> p.get("bestBefore"));
    });

    // verify created
    Arrays.stream(tenants).forEach(tenant ->
      Arrays.asList(
        WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID,
        WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID
      ).forEach(id -> assertMT.objectExists("Pizza", id, tenant.getName())));
  }

  @Test
  public void shouldNotCreateObjectsWithoutTenant() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };

    testGenerics.createSchemaPizzaForTenants(client);
    testGenerics.createTenantsPizza(client, tenants);

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

    Result<WeaviateObject> pizzaQuatroFormaggiStatus = client.data().creator()
      .withClassName("Pizza")
      .withID(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID)
      .withProperties(propsQuatroFormaggi)
      .run();

    assertMT.error(pizzaQuatroFormaggiStatus, null, 422, "has multi-tenancy enabled, but request was without tenant");

    Result<WeaviateObject> pizzaFruttiDiMareStatus = client.data().creator()
      .withClassName("Pizza")
      .withID(WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID)
      .withProperties(propsFruttiDiMare)
      .run();

    assertMT.error(pizzaFruttiDiMareStatus, null, 422, "has multi-tenancy enabled, but request was without tenant");

    // verify not created
    Arrays.stream(tenants).forEach(tenant ->
      Arrays.asList(
        WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID,
        WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID
      ).forEach(id -> assertMT.objectDoesNotExist("Pizza", id, tenant.getName())));
  }

  @Test
  public void shouldGetObjects() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenantNames);

    Arrays.stream(tenants).forEach(tenant -> {
        WeaviateTestGenerics.IDS_BY_CLASS.forEach((className, ids) -> {
          ids.forEach(id -> {
            Result<List<WeaviateObject>> getResultByClassId = client.data().objectsGetter()
              .withTenant(tenant.getName())
              .withClassName(className)
              .withID(id)
              .run();

            assertThat(getResultByClassId).isNotNull()
              .returns(false, Result::hasErrors)
              .extracting(Result::getResult).asList()
              .hasSize(1)
              .first()
              .extracting(o -> (WeaviateObject) o)
              .returns(id, WeaviateObject::getId)
              .returns(className, WeaviateObject::getClassName)
              .returns(tenant.getName(), WeaviateObject::getTenant);
          });

          Result<List<WeaviateObject>> getResultByClass = client.data().objectsGetter()
            .withTenant(tenant.getName())
            .withClassName(className)
            .run();

          assertThat(getResultByClass).isNotNull()
            .returns(false, Result::hasErrors)
            .extracting(Result::getResult).asList()
            .hasSize(ids.size())
            .extracting(o -> ((WeaviateObject) o).getId())
            .containsExactlyInAnyOrderElementsOf(ids);
        });

        Result<List<WeaviateObject>> getResultAll = client.data().objectsGetter()
          .withTenant(tenant.getName())
          .run();

        assertThat(getResultAll).isNotNull()
          .returns(false, Result::hasErrors)
          .extracting(Result::getResult).asList()
          .hasSize(WeaviateTestGenerics.IDS_ALL.size())
          .extracting(o -> ((WeaviateObject) o).getId())
          .containsExactlyInAnyOrderElementsOf(WeaviateTestGenerics.IDS_ALL);
      }
    );
  }

  @Test
  public void shouldNotGetObjectsWithoutTenant() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenantNames);

    WeaviateTestGenerics.IDS_BY_CLASS.forEach((className, ids) -> {
      ids.forEach(id -> {
        Result<List<WeaviateObject>> getResultByClassId = client.data().objectsGetter()
          .withClassName(className)
          .withID(id)
          .run();

        assertMT.error(getResultByClassId, null, 422, "has multi-tenancy enabled, but request was without tenant");
      });

      Result<List<WeaviateObject>> getResultByClass = client.data().objectsGetter()
        .withClassName(className)
        .run();

      assertMT.error(getResultByClass, null, 422, "has multi-tenancy enabled, but request was without tenant");
    });

    Result<List<WeaviateObject>> getResultAll = client.data().objectsGetter()
      .run();

    assertThat(getResultAll).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asList()
      .isEmpty();
  }

  @Test
  public void shouldCheckObjects() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenantNames);

    Arrays.stream(tenants).forEach(tenant ->
      WeaviateTestGenerics.IDS_BY_CLASS.forEach((className, ids) ->
        ids.forEach(id -> {
          Result<Boolean> checkResult = client.data().checker()
            .withClassName(className)
            .withID(id)
            .withTenant(tenant.getName())
            .run();

          assertThat(checkResult).isNotNull()
            .returns(false, Result::hasErrors)
            .returns(true, Result::getResult);
        })
      )
    );
  }

  @Test
  public void shouldNotCheckObjectsWithoutTenant() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenantNames);

    WeaviateTestGenerics.IDS_BY_CLASS.forEach((className, ids) ->
      ids.forEach(id -> {
        Result<Boolean> checkResult = client.data().checker()
          .withClassName(className)
          .withID(id)
          .run();

        assertThat(checkResult).isNotNull()
          .returns(false, Result::getResult)
          .returns(true, Result::hasErrors)
          .extracting(Result::getError)
          .returns(422, WeaviateError::getStatusCode)
          .extracting(WeaviateError::getMessages).asList()
          .isEmpty();
      })
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
        int[] expectedObjectsLeft = new int[]{ids.size()};

        ids.forEach(id -> {
          Result<Boolean> deleteStatus = client.data().deleter()
            .withTenant(tenant.getName())
            .withClassName(className)
            .withID(id)
            .run();

          assertThat(deleteStatus).isNotNull()
            .returns(false, Result::hasErrors)
            .returns(true, Result::getResult);

          // verify deleted
          assertMT.objectDoesNotExist(className, id, tenant.getName());
          assertMT.countObjects(className, tenant.getName(), --expectedObjectsLeft[0]);
        });
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

    WeaviateTestGenerics.IDS_BY_CLASS.forEach((className, ids) ->
      ids.forEach(id -> {
        Result<Boolean> deleteStatus = client.data().deleter()
          .withClassName(className)
          .withID(id)
          .run();

        assertMT.error(deleteStatus, false, 422, "has multi-tenancy enabled, but request was without tenant");

        // verify not deleted
        Arrays.stream(tenants).forEach(tenant ->
          assertMT.objectExists(className, id, tenant.getName())
        );
      })
    );

    // verify not deleted
    Arrays.stream(tenants).forEach(tenant ->
      assertMT.countObjects(tenant.getName(), WeaviateTestGenerics.IDS_ALL.size())
    );
  }

  @Test
  public void shouldUpdateObjects() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenantNames);

    Map<String, Object> propsChicken = new HashMap<>();
    propsChicken.put("name", "ChickenSoup");
    propsChicken.put("description", "updated ChickenSoup description");
    propsChicken.put("price", 1000.1f);
    propsChicken.put("bestBefore", "2022-05-06T07:08:09+05:00");

    Map<String, Object> propsBeautiful = new HashMap<>();
    propsBeautiful.put("name", "Beautiful");
    propsBeautiful.put("description", "updated Beautiful description");
    propsBeautiful.put("price", 2000.2f);
    propsBeautiful.put("bestBefore", "2022-06-07T08:09:10+06:00");

    Arrays.stream(tenants).forEach(tenant -> {
      Result<Boolean> soupChickenStatus = client.data().updater()
        .withTenant(tenant.getName())
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
        .withProperties(propsChicken)
        .run();

      assertThat(soupChickenStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);

      Result<Boolean> soupBeautifulStatus = client.data().updater()
        .withTenant(tenant.getName())
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_BEAUTIFUL_ID)
        .withProperties(propsBeautiful)
        .run();

      assertThat(soupBeautifulStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);

      // verify updated
      Result<List<WeaviateObject>> soupChicken = client.data().objectsGetter()
        .withTenant(tenant.getName())
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
        .run();

      assertThat(soupChicken).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList()
        .hasSize(1)
        .first()
        .extracting(o -> (WeaviateObject) o)
        .returns(tenant.getName(), WeaviateObject::getTenant)
        .extracting(WeaviateObject::getProperties)
        .returns(propsChicken.get("name"), p -> p.get("name"))
        .returns(propsChicken.get("description"), p -> p.get("description"))
        .returns(1000.1d, p -> p.get("price"))
        .returns(propsChicken.get("bestBefore"), p -> p.get("bestBefore"));

      Result<List<WeaviateObject>> soupBeautiful = client.data().objectsGetter()
        .withTenant(tenant.getName())
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_BEAUTIFUL_ID)
        .run();

      assertThat(soupBeautiful).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList()
        .hasSize(1)
        .first()
        .extracting(o -> (WeaviateObject) o)
        .returns(tenant.getName(), WeaviateObject::getTenant)
        .extracting(WeaviateObject::getProperties)
        .returns(propsBeautiful.get("name"), p -> p.get("name"))
        .returns(propsBeautiful.get("description"), p -> p.get("description"))
        .returns(2000.2d, p -> p.get("price"))
        .returns(propsBeautiful.get("bestBefore"), p -> p.get("bestBefore"));
    });
  }

  @Test
  public void shouldNotUpdateObjectsWithoutTenant() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenantNames);

    Map<String, Object> propsChicken = new HashMap<>();
    propsChicken.put("name", "ChickenSoup");
    propsChicken.put("description", "updated ChickenSoup description");
    propsChicken.put("price", 1000.1f);
    propsChicken.put("bestBefore", "2022-05-06T07:08:09+05:00");

    Map<String, Object> propsBeautiful = new HashMap<>();
    propsBeautiful.put("name", "Beautiful");
    propsBeautiful.put("description", "updated Beautiful description");
    propsBeautiful.put("price", 2000.2f);
    propsBeautiful.put("bestBefore", "2022-06-07T08:09:10+06:00");

    Result<Boolean> soupChickenStatus = client.data().updater()
      .withClassName("Soup")
      .withID(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
      .withProperties(propsChicken)
      .run();

    assertMT.error(soupChickenStatus, false, 422, "has multi-tenancy enabled, but request was without tenant");

    Result<Boolean> soupBeautifulStatus = client.data().updater()
      .withClassName("Soup")
      .withID(WeaviateTestGenerics.SOUP_BEAUTIFUL_ID)
      .withProperties(propsBeautiful)
      .run();

    assertMT.error(soupBeautifulStatus, false, 422, "has multi-tenancy enabled, but request was without tenant");

    // verify not updated
    Arrays.stream(tenants).forEach(tenant -> {
      Result<List<WeaviateObject>> soupChicken = client.data().objectsGetter()
        .withTenant(tenant.getName())
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
        .run();

      assertThat(soupChicken).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList()
        .hasSize(1)
        .first()
        .extracting(o -> (WeaviateObject) o)
        .returns(tenant.getName(), WeaviateObject::getTenant)
        .extracting(WeaviateObject::getProperties)
        .returns("ChickenSoup", p -> p.get("name"))
        .returns("Used by humans when their inferior genetics are attacked by microscopic organisms.", p -> p.get("description"))
        .returns(2d, p -> p.get("price"))
        .returns("2022-05-06T07:08:09+05:00", p -> p.get("bestBefore"));

      Result<List<WeaviateObject>> soupBeautiful = client.data().objectsGetter()
        .withTenant(tenant.getName())
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_BEAUTIFUL_ID)
        .run();

      assertThat(soupBeautiful).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList()
        .hasSize(1)
        .first()
        .extracting(o -> (WeaviateObject) o)
        .returns(tenant.getName(), WeaviateObject::getTenant)
        .extracting(WeaviateObject::getProperties)
        .returns("Beautiful", p -> p.get("name"))
        .returns("Putting the game of letter soups to a whole new level.", p -> p.get("description"))
        .returns(3d, p -> p.get("price"))
        .returns("2022-06-07T08:09:10+06:00", p -> p.get("bestBefore"));
    });
  }

  @Test
  public void shouldMergeObjects() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenantNames);

    Map<String, Object> propsChicken = new HashMap<>();
    propsChicken.put("description", "updated ChickenSoup description");
    propsChicken.put("price", 1000.1f);

    Map<String, Object> propsBeautiful = new HashMap<>();
    propsBeautiful.put("description", "updated Beautiful description");
    propsBeautiful.put("price", 2000.2f);

    Arrays.stream(tenants).forEach(tenant -> {
      Result<Boolean> soupChickenStatus = client.data().updater()
        .withTenant(tenant.getName())
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
        .withProperties(propsChicken)
        .withMerge()
        .run();

      assertThat(soupChickenStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);

      Result<Boolean> soupBeautifulStatus = client.data().updater()
        .withTenant(tenant.getName())
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_BEAUTIFUL_ID)
        .withProperties(propsBeautiful)
        .withMerge()
        .run();

      assertThat(soupBeautifulStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);

      // verify merged
      Result<List<WeaviateObject>> soupChicken = client.data().objectsGetter()
        .withTenant(tenant.getName())
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
        .run();

      assertThat(soupChicken).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList()
        .hasSize(1)
        .first()
        .extracting(o -> (WeaviateObject) o)
        .returns(tenant.getName(), WeaviateObject::getTenant)
        .extracting(WeaviateObject::getProperties)
        .returns("ChickenSoup", p -> p.get("name"))
        .returns(propsChicken.get("description"), p -> p.get("description"))
        .returns(1000.1d, p -> p.get("price"))
        .returns("2022-05-06T07:08:09+05:00", p -> p.get("bestBefore"));

      Result<List<WeaviateObject>> soupBeautiful = client.data().objectsGetter()
        .withTenant(tenant.getName())
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_BEAUTIFUL_ID)
        .run();

      assertThat(soupBeautiful).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList()
        .hasSize(1)
        .first()
        .extracting(o -> (WeaviateObject) o)
        .returns(tenant.getName(), WeaviateObject::getTenant)
        .extracting(WeaviateObject::getProperties)
        .returns("Beautiful", p -> p.get("name"))
        .returns(propsBeautiful.get("description"), p -> p.get("description"))
        .returns(2000.2d, p -> p.get("price"))
        .returns("2022-06-07T08:09:10+06:00", p -> p.get("bestBefore"));
    });
  }

  @Test
  public void shouldNotMergeObjectsWithoutTenant() {
    Tenant[] tenants = new Tenant[]{
      WeaviateTestGenerics.TENANT_1,
      WeaviateTestGenerics.TENANT_2,
    };
    String[] tenantNames = Arrays.stream(tenants).map(Tenant::getName).toArray(String[]::new);
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createTenantsSoup(client, tenants);
    testGenerics.createDataSoupForTenants(client, tenantNames);

    Map<String, Object> propsChicken = new HashMap<>();
    propsChicken.put("description", "updated ChickenSoup description");
    propsChicken.put("price", 1000.1f);

    Map<String, Object> propsBeautiful = new HashMap<>();
    propsBeautiful.put("description", "updated Beautiful description");
    propsBeautiful.put("price", 2000.2f);

    Result<Boolean> soupChickenStatus = client.data().updater()
      .withClassName("Soup")
      .withID(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
      .withProperties(propsChicken)
      .withMerge()
      .run();

    assertMT.error(soupChickenStatus, false, 422, "has multi-tenancy enabled, but request was without tenant");

    Result<Boolean> soupBeautifulStatus = client.data().updater()
      .withClassName("Soup")
      .withID(WeaviateTestGenerics.SOUP_BEAUTIFUL_ID)
      .withProperties(propsBeautiful)
      .withMerge()
      .run();

    assertMT.error(soupBeautifulStatus, false, 422, "has multi-tenancy enabled, but request was without tenant");

    // verify not updated
    Arrays.stream(tenants).forEach(tenant -> {
      Result<List<WeaviateObject>> soupChicken = client.data().objectsGetter()
        .withTenant(tenant.getName())
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
        .run();

      assertThat(soupChicken).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList()
        .hasSize(1)
        .first()
        .extracting(o -> (WeaviateObject) o)
        .returns(tenant.getName(), WeaviateObject::getTenant)
        .extracting(WeaviateObject::getProperties)
        .returns("ChickenSoup", p -> p.get("name"))
        .returns("Used by humans when their inferior genetics are attacked by microscopic organisms.", p -> p.get("description"))
        .returns(2d, p -> p.get("price"))
        .returns("2022-05-06T07:08:09+05:00", p -> p.get("bestBefore"));

      Result<List<WeaviateObject>> soupBeautiful = client.data().objectsGetter()
        .withTenant(tenant.getName())
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_BEAUTIFUL_ID)
        .run();

      assertThat(soupBeautiful).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList()
        .hasSize(1)
        .first()
        .extracting(o -> (WeaviateObject) o)
        .returns(tenant.getName(), WeaviateObject::getTenant)
        .extracting(WeaviateObject::getProperties)
        .returns("Beautiful", p -> p.get("name"))
        .returns("Putting the game of letter soups to a whole new level.", p -> p.get("description"))
        .returns(3d, p -> p.get("price"))
        .returns("2022-06-07T08:09:10+06:00", p -> p.get("bestBefore"));
    });
  }
}
