package io.weaviate.integration.client.data;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.base.WeaviateError;
import io.weaviate.client.base.WeaviateErrorMessage;
import io.weaviate.client.v1.batch.api.ReferencePayloadBuilder;
import io.weaviate.client.v1.batch.model.BatchDeleteResponse;
import io.weaviate.client.v1.batch.model.BatchReference;
import io.weaviate.client.v1.batch.model.BatchReferenceResponse;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.batch.model.ObjectsGetResponseAO2Result;
import io.weaviate.client.v1.data.model.SingleRef;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.integration.client.WeaviateTestGenerics;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

public class ClientDataMultiTenancyTest {
  private WeaviateClient client;
  private WeaviateTestGenerics testGenerics;

  private static final Map<String, List<String>> IDS_BY_CLASS = new HashMap<>();

  static {
    IDS_BY_CLASS.put("Pizza", Arrays.asList(
      WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID,
      WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID,
      WeaviateTestGenerics.PIZZA_HAWAII_ID,
      WeaviateTestGenerics.PIZZA_DOENER_ID
    ));
    IDS_BY_CLASS.put("Soup", Arrays.asList(
      WeaviateTestGenerics.SOUP_CHICKENSOUP_ID,
      WeaviateTestGenerics.SOUP_BEAUTIFUL_ID
    ));
  }

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
  public void shouldAddObjects() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2", "TenantNo3"};
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);

    Map<String, Object> pizzaProps = new HashMap<>();
    pizzaProps.put("name", "Quattro Formaggi");
    pizzaProps.put("description", "Pizza quattro formaggi Italian: [ˈkwattro forˈmaddʒi] (four cheese pizza) is a variety of pizza in Italian cuisine that is topped with a combination of four kinds of cheese, usually melted together, with (rossa, red) or without (bianca, white) tomato sauce. It is popular worldwide, including in Italy,[1] and is one of the iconic items from pizzerias's menus.");
    pizzaProps.put("price", 1.4f);
    pizzaProps.put("bestBefore", "2022-01-02T03:04:05+01:00");

    Map<String, Object> soupProps = new HashMap<>();
    soupProps.put("name", "ChickenSoup");
    soupProps.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
    soupProps.put("price", 2.0f);
    soupProps.put("bestBefore", "2022-05-06T07:08:09+05:00");

    Arrays.stream(tenants).forEach(tenant -> {
      pizzaProps.put("tenantName", tenant);

      Result<WeaviateObject> pizzaCreateStatus = client.data().creator()
        .withClassName("Pizza")
        .withID(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID)
        .withProperties(pizzaProps)
        .withTenantKey(tenant)
        .run();

      assertThat(pizzaCreateStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult)
        .returns(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID, WeaviateObject::getId)
        .returns("Pizza", WeaviateObject::getClassName)
        .extracting(WeaviateObject::getProperties)
        .returns("Quattro Formaggi", p -> p.get("name"))
        .returns(1.4d, p -> p.get("price"))
        .returns("Pizza quattro formaggi Italian: [ˈkwattro forˈmaddʒi] (four cheese pizza) is a variety of pizza in Italian cuisine that is topped with a combination of four kinds of cheese, usually melted together, with (rossa, red) or without (bianca, white) tomato sauce. It is popular worldwide, including in Italy,[1] and is one of the iconic items from pizzerias's menus.", p -> p.get("description"))
        .returns("2022-01-02T03:04:05+01:00", p -> p.get("bestBefore"))
        .returns(tenant, p -> p.get("tenantName"));

      soupProps.put("tenantName", tenant);

      Result<WeaviateObject> soupCreateStatus = client.data().creator()
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
        .withProperties(soupProps)
        .withTenantKey(tenant)
        .run();

      assertThat(soupCreateStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult)
        .returns(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID, WeaviateObject::getId)
        .returns("Soup", WeaviateObject::getClassName)
        .extracting(WeaviateObject::getProperties)
        .returns("ChickenSoup", p -> p.get("name"))
        .returns(2.0d, p -> p.get("price"))
        .returns("Used by humans when their inferior genetics are attacked by microscopic organisms.", p -> p.get("description"))
        .returns("2022-05-06T07:08:09+05:00", p -> p.get("bestBefore"))
        .returns(tenant, p -> p.get("tenantName"));
    });
  }

  @Test
  public void shouldCheckObjects() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2", "TenantNo3"};
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenants);

    Arrays.stream(tenants).forEach(tenant ->
      IDS_BY_CLASS.forEach((className, classIds) ->
        classIds.forEach(id -> {
          Result<Boolean> checkResult = client.data().checker()
            .withTenantKey(tenant)
            .withClassName(className)
            .withID(id)
            .run();

          assertThat(checkResult).isNotNull()
            .returns(false, Result::hasErrors)
            .returns(true, Result::getResult);
        })
      )
    );
  }

  @Test
  public void shouldGetObjects() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2", "TenantNo3"};
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenants);

    Arrays.stream(tenants).forEach(tenant ->
        IDS_BY_CLASS.forEach((className, classIds) -> {
          // TODO should fetch all for tenant?
//        Result<List<WeaviateObject>> getResult = client.data().objectsGetter()
//          .withTenantKey(tenant)
//          .withClassName(className)
//          .run();
//
//        assertThat(getResult).isNotNull()
//          .returns(false, Result::hasErrors);

          classIds.forEach(id -> {
            Result<List<WeaviateObject>> getOneResult = client.data().objectsGetter()
              .withTenantKey(tenant)
              .withClassName(className)
              .withID(id)
              .run();

            assertThat(getOneResult).isNotNull()
              .returns(false, Result::hasErrors)
              .extracting(Result::getResult).asList()
              .hasSize(1)
              .first()
              .extracting(o -> (WeaviateObject) o)
              .returns(id, WeaviateObject::getId)
              .returns(className, WeaviateObject::getClassName)
              .extracting(WeaviateObject::getProperties)
              .returns(tenant, p -> p.get("tenantName"));
          });
        })
    );
  }

  @Test
  public void shouldUpdateObjects() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2", "TenantNo3"};
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenants);

    Map<String, Object> pizzaProps = new HashMap<>();
    pizzaProps.put("name", "Quattro Formaggi");
    pizzaProps.put("description", "updated Quattro Formaggi description");
    pizzaProps.put("price", 1000.1f);
    pizzaProps.put("bestBefore", "2022-01-02T03:04:05+01:00");

    Map<String, Object> soupProps = new HashMap<>();
    soupProps.put("name", "ChickenSoup");
    soupProps.put("description", "updated ChickenSoup description");
    soupProps.put("price", 2000.2f);
    soupProps.put("bestBefore", "2022-05-06T07:08:09+05:00");

    Arrays.stream(tenants).forEach(tenant -> {
      pizzaProps.put("tenantName", tenant);

      Result<Boolean> pizzaUpdateStatus = client.data().updater()
        .withClassName("Pizza")
        .withID(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID)
        .withProperties(pizzaProps)
        .withTenantKey(tenant)
        .run();

      assertThat(pizzaUpdateStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);

      Result<List<WeaviateObject>> getPizzaResult = client.data().objectsGetter()
        .withTenantKey(tenant)
        .withClassName("Pizza")
        .withID(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID)
        .run();

      assertThat(getPizzaResult).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList()
        .hasSize(1)
        .first()
        .extracting(o -> ((WeaviateObject) o).getProperties())
        .returns("Quattro Formaggi", p -> p.get("name"))
        .returns("updated Quattro Formaggi description", p -> p.get("description"))
        .returns(1000.1d, p -> p.get("price"))
        .returns("2022-01-02T03:04:05+01:00", p -> p.get("bestBefore"))
        .returns(tenant, p -> p.get("tenantName"));

      soupProps.put("tenantName", tenant);

      Result<Boolean> soupUpdateStatus = client.data().updater()
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
        .withProperties(soupProps)
        .withTenantKey(tenant)
        .run();

      assertThat(soupUpdateStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);

      Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
        .withTenantKey(tenant)
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
        .run();

      assertThat(getSoupResult).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList()
        .hasSize(1)
        .first()
        .extracting(o -> ((WeaviateObject) o).getProperties())
        .returns("ChickenSoup", p -> p.get("name"))
        .returns("updated ChickenSoup description", p -> p.get("description"))
        .returns(2000.2d, p -> p.get("price"))
        .returns("2022-05-06T07:08:09+05:00", p -> p.get("bestBefore"))
        .returns(tenant, p -> p.get("tenantName"));
    });
  }

  @Test
  public void shouldMergeObjects() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2", "TenantNo3"};
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenants);

    Map<String, Object> pizzaProps = new HashMap<>();
    pizzaProps.put("description", "updated Quattro Formaggi description");
    pizzaProps.put("price", 1000.1f);

    Map<String, Object> soupProps = new HashMap<>();
    soupProps.put("description", "updated ChickenSoup description");
    soupProps.put("price", 2000.2f);

    Arrays.stream(tenants).forEach(tenant -> {
      Result<Boolean> pizzaUpdateStatus = client.data().updater()
        .withClassName("Pizza")
        .withID(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID)
        .withProperties(pizzaProps)
        .withTenantKey(tenant)
        .withMerge()
        .run();

      assertThat(pizzaUpdateStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);

      Result<List<WeaviateObject>> getPizzaResult = client.data().objectsGetter()
        .withTenantKey(tenant)
        .withClassName("Pizza")
        .withID(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID)
        .run();

      assertThat(getPizzaResult).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList()
        .hasSize(1)
        .first()
        .extracting(o -> ((WeaviateObject) o).getProperties())
        .returns("Quattro Formaggi", p -> p.get("name"))
        .returns("updated Quattro Formaggi description", p -> p.get("description"))
        .returns(1000.1d, p -> p.get("price"))
        .returns("2022-01-02T03:04:05+01:00", p -> p.get("bestBefore"))
        .returns(tenant, p -> p.get("tenantName"));

      Result<Boolean> soupUpdateStatus = client.data().updater()
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
        .withProperties(soupProps)
        .withTenantKey(tenant)
        .withMerge()
        .run();

      assertThat(soupUpdateStatus).isNotNull()
        .returns(false, Result::hasErrors)
        .returns(true, Result::getResult);

      Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
        .withTenantKey(tenant)
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
        .run();

      assertThat(getSoupResult).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList()
        .hasSize(1)
        .first()
        .extracting(o -> ((WeaviateObject) o).getProperties())
        .returns("ChickenSoup", p -> p.get("name"))
        .returns("updated ChickenSoup description", p -> p.get("description"))
        .returns(2000.2d, p -> p.get("price"))
        .returns("2022-05-06T07:08:09+05:00", p -> p.get("bestBefore"))
        .returns(tenant, p -> p.get("tenantName"));
    });
  }

  @Test
  public void shouldDeleteObjects() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2", "TenantNo3"};
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenants);

    Arrays.stream(tenants).forEach(tenant ->
      IDS_BY_CLASS.forEach((className, classIds) ->
        classIds.forEach(id -> {
          Result<Boolean> deleteStatus = client.data().deleter()
            .withTenantKey(tenant)
            .withClassName(className)
            .withID(id)
            .run();

          assertThat(deleteStatus).isNotNull()
            .returns(false, Result::hasErrors)
            .returns(true, Result::getResult);

          Result<List<WeaviateObject>> getOneResult = client.data().objectsGetter()
            .withTenantKey(tenant)
            .withClassName(className)
            .withID(id)
            .run();

          assertThat(getOneResult).isNotNull()
            .returns(false, Result::hasErrors)
            .extracting(Result::getResult).isNull();
        })
      )
    );
  }

  @Test
  public void shouldBatchAddObjects() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2", "TenantNo3"};
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);

    Arrays.stream(tenants).forEach(tenant -> {
      Map<String, Object> pizzaProps = new HashMap<>();
      pizzaProps.put("name", "Quattro Formaggi");
      pizzaProps.put("description", "Pizza quattro formaggi Italian: [ˈkwattro forˈmaddʒi] (four cheese pizza) is a variety of pizza in Italian cuisine that is topped with a combination of four kinds of cheese, usually melted together, with (rossa, red) or without (bianca, white) tomato sauce. It is popular worldwide, including in Italy,[1] and is one of the iconic items from pizzerias's menus.");
      pizzaProps.put("price", 1.4f);
      pizzaProps.put("bestBefore", "2022-01-02T03:04:05+01:00");
      pizzaProps.put("tenantName", tenant);

      WeaviateObject pizza = WeaviateObject.builder()
        .id(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID)
        .className("Pizza")
        .properties(pizzaProps)
        .build();

      Map<String, Object> soupProps = new HashMap<>();
      soupProps.put("name", "ChickenSoup");
      soupProps.put("description", "Used by humans when their inferior genetics are attacked by microscopic organisms.");
      soupProps.put("price", 2.0f);
      soupProps.put("bestBefore", "2022-05-06T07:08:09+05:00");
      soupProps.put("tenantName", tenant);

      WeaviateObject soup = WeaviateObject.builder()
        .id(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
        .className("Soup")
        .properties(soupProps)
        .build();

      Result<ObjectGetResponse[]> batchAddResult = client.batch().objectsBatcher()
        .withObjects(pizza, soup)
        .withTenantKey(tenant)
        .run();

      assertThat(batchAddResult).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asInstanceOf(ARRAY)
        .hasSize(2);
      Arrays.stream(batchAddResult.getResult()).forEach(r ->
        assertThat(r.getResult())
          // TODO should be SUCCESS
//          .returns("SUCCESS", ObjectsGetResponseAO2Result::getStatus)
          .extracting(ObjectsGetResponseAO2Result::getErrors)
          .isNull()
      );
    });

    Arrays.stream(tenants).forEach(tenant -> {
      Result<List<WeaviateObject>> getPizzaResult = client.data().objectsGetter()
        .withTenantKey(tenant)
        .withClassName("Pizza")
        .withID(WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID)
        .run();

      assertThat(getPizzaResult).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult)
        .isNotNull();

      Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
        .withTenantKey(tenant)
        .withClassName("Soup")
        .withID(WeaviateTestGenerics.SOUP_CHICKENSOUP_ID)
        .run();

      assertThat(getSoupResult).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult)
        .isNotNull();
    });
  }

  @Test
  public void shouldBatchDeleteObjects() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2", "TenantNo3"};
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenants);

    Arrays.stream(tenants).forEach(tenant ->
      IDS_BY_CLASS.forEach((className, classIds) -> {
        Result<BatchDeleteResponse> batchDeleteResult = client.batch().objectsBatchDeleter()
          .withClassName(className)
          .withTenantKey(tenant)
          .withWhere(WhereFilter.builder()
            .operator(Operator.Like)
            .path(new String[]{"_id"})
            .valueText("*")
            .build())
          .run();

        assertThat(batchDeleteResult).isNotNull()
          .returns(false, Result::hasErrors)
          .extracting(Result::getResult).isNotNull()
          .extracting(BatchDeleteResponse::getResults)
          .returns((long) classIds.size(), BatchDeleteResponse.Results::getMatches)
          .returns((long) classIds.size(), BatchDeleteResponse.Results::getSuccessful);

        classIds.forEach(id -> {
          Result<List<WeaviateObject>> getOneResult = client.data().objectsGetter()
            .withTenantKey(tenant)
            .withClassName(className)
            .withID(id)
            .run();

          assertThat(getOneResult).isNotNull()
            .returns(false, Result::hasErrors)
            .extracting(Result::getResult)
            .isNull();
        });
      })
    );
  }

  @Test
  public void shouldAddReferences() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2", "TenantNo3"};
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenants);

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

    Arrays.stream(tenants).forEach(tenant ->
      IDS_BY_CLASS.get("Soup").forEach(soupId -> {
        IDS_BY_CLASS.get("Pizza").forEach(pizzaId -> {
          SingleRef pizzaRef = client.data().referencePayloadBuilder()
            .withClassName("Pizza")
            .withID(pizzaId)
            .payload();

          Result<Boolean> refAddResult = client.data().referenceCreator()
            .withClassName("Soup")
            .withID(soupId)
            .withReferenceProperty("relatedToPizza")
            .withReference(pizzaRef)
            .withTenantKey(tenant)
            .run();

          assertThat(refAddResult).isNotNull()
            .returns(false, Result::hasErrors)
            .returns(true, Result::getResult);
        });

        Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
          .withTenantKey(tenant)
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
          .hasSize(IDS_BY_CLASS.get("Pizza").size());
      })
    );
  }

  @Test
  @Ignore("reference replace seems to be broken for now")
  public void shouldReplaceReferences() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2", "TenantNo3"};
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenants);

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

    Arrays.stream(tenants).forEach(tenant ->
      IDS_BY_CLASS.get("Soup").forEach(soupId -> {
        ReferencePayloadBuilder soupRpb = client.batch().referencePayloadBuilder()
          .withFromClassName("Soup")
          .withFromID(soupId)
          .withFromRefProp("relatedToPizza")
          .withToClassName("Pizza");
        BatchReference[] refs = IDS_BY_CLASS.get("Pizza").stream()
          .skip(2)
          .map(pizzaId -> soupRpb.withToID(pizzaId).payload())
          .toArray(BatchReference[]::new);

        Result<BatchReferenceResponse[]> batchRefResult = client.batch().referencesBatcher()
          .withReferences(refs)
          .withTenantKey(tenant)
          .run();

        assertThat(batchRefResult).isNotNull()
          .returns(false, Result::hasErrors);

        io.weaviate.client.v1.data.builder.ReferencePayloadBuilder pizzaRpb = client.data().referencePayloadBuilder()
          .withClassName("Pizza");
        SingleRef[] replaceRefs = IDS_BY_CLASS.get("Pizza").stream()
          .limit(2)
          .map(pizzaId -> pizzaRpb.withID(pizzaId).payload())
          .toArray(SingleRef[]::new);

        Result<Boolean> refReplaceResult = client.data().referenceReplacer()
          .withClassName("Soup")
          .withID(soupId)
          .withReferenceProperty("relatedToPizza")
          .withReferences(replaceRefs)
          .withTenantKey(tenant)
          .run();

        assertThat(refReplaceResult).isNotNull()
          .returns(false, Result::hasErrors)
          .returns(true, Result::getResult);

        Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
          .withTenantKey(tenant)
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
          .hasSize(2);
      })
    );
  }

  @Test
  public void shouldRemoveReferences() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2", "TenantNo3"};
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenants);

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

    Arrays.stream(tenants).forEach(tenant ->
      IDS_BY_CLASS.get("Soup").forEach(soupId -> {
        ReferencePayloadBuilder rpb = client.batch().referencePayloadBuilder()
          .withFromClassName("Soup")
          .withFromID(soupId)
          .withFromRefProp("relatedToPizza")
          .withToClassName("Pizza");
        BatchReference[] references = IDS_BY_CLASS.get("Pizza").stream().map(pizzaId ->
          rpb.withToID(pizzaId).payload()
        ).toArray(BatchReference[]::new);

        Result<BatchReferenceResponse[]> batchRefResult = client.batch().referencesBatcher()
          .withReferences(references)
          .withTenantKey(tenant)
          .run();

        assertThat(batchRefResult).isNotNull()
          .returns(false, Result::hasErrors);

        int[] refsLeft = new int[]{IDS_BY_CLASS.get("Pizza").size()};
        IDS_BY_CLASS.get("Pizza").forEach(pizzaId -> {
          SingleRef pizzaRef = client.data().referencePayloadBuilder()
            .withClassName("Pizza")
            .withID(pizzaId)
            .payload();

          Result<Boolean> refDeleteResult = client.data().referenceDeleter()
            .withClassName("Soup")
            .withID(soupId)
            .withReferenceProperty("relatedToPizza")
            .withReference(pizzaRef)
            .withTenantKey(tenant)
            .run();

          assertThat(refDeleteResult).isNotNull()
            .returns(false, Result::hasErrors)
            .returns(true, Result::getResult);

          Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
            .withTenantKey(tenant)
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
            .hasSize(--refsLeft[0]);
        });
      })
    );
  }

  @Test
  public void shouldBatchAddReferences() {
    String[] tenants = new String[]{"TenantNo1", "TenantNo2", "TenantNo3"};
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenants);
    testGenerics.createDataFoodForTenants(client, tenants);

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

    Arrays.stream(tenants).forEach(tenant ->
      IDS_BY_CLASS.get("Soup").forEach(soupId -> {
        ReferencePayloadBuilder rpb = client.batch().referencePayloadBuilder()
          .withFromClassName("Soup")
          .withFromID(soupId)
          .withFromRefProp("relatedToPizza")
          .withToClassName("Pizza");
        BatchReference[] references = IDS_BY_CLASS.get("Pizza").stream().map(pizzaId ->
          rpb.withToID(pizzaId).payload()
        ).toArray(BatchReference[]::new);

        Result<BatchReferenceResponse[]> batchRefResult = client.batch().referencesBatcher()
          .withReferences(references)
          .withTenantKey(tenant)
          .run();

        assertThat(batchRefResult).isNotNull()
          .returns(false, Result::hasErrors)
          .extracting(Result::getResult).asInstanceOf(ARRAY)
          .hasSize(IDS_BY_CLASS.get("Pizza").size());

        Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
          .withTenantKey(tenant)
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
          .hasSize(IDS_BY_CLASS.get("Pizza").size());
      })
    );
  }

  @Test
  public void shouldNotAddReferenceBetweenTenants() {
    String tenantSoup = "TenantSoup";
    String tenantPizza = "TenantPizza";
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenantSoup, tenantPizza);
    testGenerics.createDataSoupForTenants(client, tenantSoup);
    testGenerics.createDataPizzaForTenants(client, tenantPizza);

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

    IDS_BY_CLASS.get("Soup").forEach(soupId -> {
      IDS_BY_CLASS.get("Pizza").forEach(pizzaId -> {
        SingleRef pizzaRef = client.data().referencePayloadBuilder()
          .withClassName("Pizza")
          .withID(pizzaId)
          .payload();

        Result<Boolean> refAddResultTenantSoup = client.data().referenceCreator()
          .withClassName("Soup")
          .withID(soupId)
          .withReferenceProperty("relatedToPizza")
          .withReference(pizzaRef)
          .withTenantKey(tenantSoup)
          .run();

        assertThat(refAddResultTenantSoup).isNotNull()
          .returns(true, Result::hasErrors)
          .extracting(Result::getError)
          .extracting(WeaviateError::getMessages).asList()
          .first()
          .extracting(m -> ((WeaviateErrorMessage) m).getMessage()).asInstanceOf(STRING)
          .contains("no object with id", "found");

        Result<Boolean> refAddResultTenantPizza = client.data().referenceCreator()
          .withClassName("Soup")
          .withID(soupId)
          .withReferenceProperty("relatedToPizza")
          .withReference(pizzaRef)
          .withTenantKey(tenantPizza)
          .run();

        assertThat(refAddResultTenantPizza).isNotNull()
          .returns(false, Result::hasErrors);
        assertThat(refAddResultTenantPizza).isNotNull()
          .returns(false, Result::getResult);

        Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
          .withTenantKey(tenantSoup)
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
    });
  }

  @Test
  @Ignore("adding batch references between different tenants should not be possible")
  public void shouldNotBatchAddReferenceBetweenTenants() {
    String tenantSoup = "TenantSoup";
    String tenantPizza = "TenantPizza";
    testGenerics.createSchemaFoodForTenants(client);
    testGenerics.createTenantsFood(client, tenantSoup, tenantPizza);
    testGenerics.createDataSoupForTenants(client, tenantSoup);
    testGenerics.createDataPizzaForTenants(client, tenantPizza);

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

    IDS_BY_CLASS.get("Soup").forEach(soupId -> {
      ReferencePayloadBuilder rpb = client.batch().referencePayloadBuilder()
        .withFromClassName("Soup")
        .withFromID(soupId)
        .withFromRefProp("relatedToPizza")
        .withToClassName("Pizza");
      BatchReference[] references = IDS_BY_CLASS.get("Pizza").stream().map(pizzaId ->
        rpb.withToID(pizzaId).payload()
      ).toArray(BatchReference[]::new);

      Result<BatchReferenceResponse[]> batchRefResult = client.batch().referencesBatcher()
        .withReferences(references)
        .withTenantKey(tenantSoup)
        .run();

      // TODO should error
      assertThat(batchRefResult).isNotNull()
        .returns(true, Result::hasErrors);

      Result<BatchReferenceResponse[]> batchRefResult2 = client.batch().referencesBatcher()
        .withReferences(references)
        .withTenantKey(tenantPizza)
        .run();

      // TODO should error
      assertThat(batchRefResult2).isNotNull()
        .returns(true, Result::hasErrors);

      Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
        .withTenantKey(tenantSoup)
        .withClassName("Soup")
        .withID(soupId)
        .run();

      // TODO should be empty
      assertThat(getSoupResult).isNotNull()
        .returns(false, Result::hasErrors)
        .extracting(Result::getResult).asList()
        .hasSize(1)
        .first()
        .extracting(o -> ((WeaviateObject) o).getProperties())
        .extracting(p -> p.get("relatedToPizza"))
        .isNull();

      Result<List<WeaviateObject>> getSoupResult2 = client.data().objectsGetter()
        .withTenantKey(tenantPizza)
        .withClassName("Soup")
        .withID(soupId)
        .run();

      // TODO should be empty
      assertThat(getSoupResult2).isNotNull()
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
  public void shouldAddReferenceBetweenTenantAndNonTenant() {
    String tenantSoup = "TenantSoup";
    testGenerics.createSchemaSoupForTenants(client);
    testGenerics.createSchemaPizza(client);
    testGenerics.createTenantsSoup(client, tenantSoup);
    testGenerics.createDataSoupForTenants(client, tenantSoup);
    testGenerics.createDataPizza(client);

    Result<Boolean> refPropSoupResult = client.schema().propertyCreator()
      .withClassName("Soup")
      .withProperty(Property.builder()
        .name("relatedToPizza")
        .dataType(Collections.singletonList("Pizza"))
        .build())
      .run();

    assertThat(refPropSoupResult).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    Result<Boolean> refPropPizzaResult = client.schema().propertyCreator()
      .withClassName("Pizza")
      .withProperty(Property.builder()
        .name("relatedToSoup")
        .dataType(Collections.singletonList("Soup"))
        .build())
      .run();

    assertThat(refPropPizzaResult).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);

    int[] soupCounter = new int[]{0};
    IDS_BY_CLASS.get("Soup").forEach(soupId -> {
      ++soupCounter[0];

      SingleRef soupRef = client.data().referencePayloadBuilder()
        .withClassName("Soup")
        .withID(soupId)
        .payload();

      IDS_BY_CLASS.get("Pizza").forEach(pizzaId -> {
        SingleRef pizzaRef = client.data().referencePayloadBuilder()
          .withClassName("Pizza")
          .withID(pizzaId)
          .payload();

        Result<Boolean> refAddResultTenantSoup = client.data().referenceCreator()
          .withClassName("Soup")
          .withID(soupId)
          .withReferenceProperty("relatedToPizza")
          .withReference(pizzaRef)
          .withTenantKey(tenantSoup)
          .run();

        assertThat(refAddResultTenantSoup).isNotNull()
          .returns(false, Result::hasErrors)
          .returns(true, Result::getResult);

        Result<Boolean> refAddResultTenantPizza = client.data().referenceCreator()
          .withClassName("Pizza")
          .withID(pizzaId)
          .withReferenceProperty("relatedToSoup")
          .withReference(soupRef)
          .withTenantKey(tenantSoup)
          .run();

        assertThat(refAddResultTenantPizza).isNotNull()
          .returns(false, Result::hasErrors)
          .returns(true, Result::getResult);

        Result<List<WeaviateObject>> getPizzaResult = client.data().objectsGetter()
          .withClassName("Pizza")
          .withID(pizzaId)
          .run();

        assertThat(getPizzaResult).isNotNull()
          .returns(false, Result::hasErrors)
          .extracting(Result::getResult).asList()
          .hasSize(1)
          .first()
          .extracting(o -> ((WeaviateObject) o).getProperties())
          .extracting(p -> p.get("relatedToSoup")).asList()
          .hasSize(soupCounter[0]);
      });

      Result<List<WeaviateObject>> getSoupResult = client.data().objectsGetter()
        .withTenantKey(tenantSoup)
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
        .hasSize(IDS_BY_CLASS.get("Pizza").size());
    });
  }
}

