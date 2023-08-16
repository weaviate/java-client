package io.weaviate.integration.client;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.batch.model.ObjectGetResponse;
import io.weaviate.client.v1.data.model.SingleRef;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.misc.model.InvertedIndexConfig;
import io.weaviate.client.v1.misc.model.MultiTenancyConfig;
import io.weaviate.client.v1.misc.model.ReplicationConfig;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Tenant;
import io.weaviate.client.v1.schema.model.Tokenization;
import io.weaviate.client.v1.schema.model.WeaviateClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.ARRAY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WeaviateTestGenerics {

  public static final String PIZZA_QUATTRO_FORMAGGI_ID = "10523cdd-15a2-42f4-81fa-267fe92f7cd6";
  public static final String PIZZA_FRUTTI_DI_MARE_ID = "927dd3ac-e012-4093-8007-7799cc7e81e4";
  public static final String PIZZA_HAWAII_ID = "00000000-0000-0000-0000-000000000000";
  public static final String PIZZA_DOENER_ID = "d2b393ff-4b26-48c7-b554-218d970a9e17";
  public static final String SOUP_CHICKENSOUP_ID = "8c156d37-81aa-4ce9-a811-621e2702b825";
  public static final String SOUP_BEAUTIFUL_ID = "27351361-2898-4d1a-aad7-1ca48253eb0b";

  public static final Map<String, List<String>> IDS_BY_CLASS = new HashMap<>();
  public static final List<String> IDS_ALL = Arrays.asList(
    WeaviateTestGenerics.PIZZA_QUATTRO_FORMAGGI_ID,
    WeaviateTestGenerics.PIZZA_FRUTTI_DI_MARE_ID,
    WeaviateTestGenerics.PIZZA_HAWAII_ID,
    WeaviateTestGenerics.PIZZA_DOENER_ID,
    WeaviateTestGenerics.SOUP_CHICKENSOUP_ID,
    WeaviateTestGenerics.SOUP_BEAUTIFUL_ID
  );

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

  public static final Tenant TENANT_1 = Tenant.builder()
    .name("TenantNo1")
    .build();
  public static final Tenant TENANT_2 = Tenant.builder()
    .name("TenantNo2")
    .build();


  public void createWeaviateTestSchemaFood(WeaviateClient client) {
    createWeaviateTestSchemaFood(client, false);
  }

  public void createWeaviateTestSchemaFood(WeaviateClient client, boolean deprecatedMode) {
    // classes
    WeaviateClass pizza = WeaviateClass.builder()
      .className("Pizza")
      .description("A delicious religion like food and arguably the best export of Italy.")
      .invertedIndexConfig(InvertedIndexConfig.builder().indexTimestamps(true).build())
      .build();
    WeaviateClass soup = WeaviateClass.builder()
      .className("Soup")
      .description("Mostly water based brew of sustenance for humans.")
      .build();
    // create Pizza class
    Result<Boolean> pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run();
    assertNotNull(pizzaCreateStatus);
    assertTrue(pizzaCreateStatus.getResult());
    // create Soup class
    Result<Boolean> soupCreateStatus = client.schema().classCreator().withClass(soup).run();
    assertNotNull(soupCreateStatus);
    assertTrue(soupCreateStatus.getResult());

    // properties
    Property nameProperty = deprecatedMode
      ? Property.builder()
      .dataType(Arrays.asList(DataType.STRING))
      .description("name")
      .name("name")
      .tokenization(Tokenization.FIELD)
      .build()
      : Property.builder()
      .dataType(Arrays.asList(DataType.TEXT))
      .description("name")
      .name("name")
      .tokenization(Tokenization.FIELD)
      .build();
    Property descriptionProperty = Property.builder()
      .dataType(Arrays.asList(DataType.TEXT))
      .description("description")
      .name("description")
      .tokenization(Tokenization.WORD)
      .build();
    Property bestBeforeProperty = Property.builder()
      .dataType(Arrays.asList(DataType.DATE))
      .description("best before")
      .name("bestBefore")
      .build();
    Map<Object, Object> text2vecContextionary = new HashMap<>();
    text2vecContextionary.put("skip", true);
    Map<Object, Object> moduleConfig = new HashMap<>();
    moduleConfig.put("text2vec-contextionary", text2vecContextionary);
    Property priceProperty = Property.builder()
      .dataType(Arrays.asList(DataType.NUMBER))
      .description("price")
      .name("price")
      .moduleConfig(moduleConfig)
      .build();
    // Add name and description properties to Pizza
    Result<Boolean> pizzaPropertyNameStatus = client.schema().propertyCreator()
      .withProperty(nameProperty).withClassName(pizza.getClassName()).run();
    assertNotNull(pizzaPropertyNameStatus);
    assertTrue(pizzaPropertyNameStatus.getResult());
    Result<Boolean> pizzaPropertyDescriptionStatus = client.schema().propertyCreator()
      .withProperty(descriptionProperty).withClassName(pizza.getClassName()).run();
    assertNotNull(pizzaPropertyDescriptionStatus);
    assertTrue(pizzaPropertyDescriptionStatus.getResult());
    Result<Boolean> pizzaPropertyBestBeforeStatus = client.schema().propertyCreator()
      .withProperty(bestBeforeProperty).withClassName(pizza.getClassName()).run();
    assertNotNull(pizzaPropertyBestBeforeStatus);
    assertTrue(pizzaPropertyBestBeforeStatus.getResult());
    Result<Boolean> pizzaPropertyPriceStatus = client.schema().propertyCreator()
      .withProperty(priceProperty).withClassName(pizza.getClassName()).run();
    assertNotNull(pizzaPropertyPriceStatus);
    assertTrue(pizzaPropertyPriceStatus.getResult());
    // Add name and description properties to Soup
    Result<Boolean> soupPropertyNameStatus = client.schema().propertyCreator()
      .withProperty(nameProperty).withClassName(soup.getClassName()).run();
    assertNotNull(soupPropertyNameStatus);
    assertTrue(soupPropertyNameStatus.getResult());
    Result<Boolean> soupPropertyDescriptionStatus = client.schema().propertyCreator()
      .withProperty(descriptionProperty).withClassName(soup.getClassName()).run();
    assertNotNull(soupPropertyDescriptionStatus);
    assertTrue(soupPropertyDescriptionStatus.getResult());
    Result<Boolean> soupPropertyBestBeforeStatus = client.schema().propertyCreator()
      .withProperty(bestBeforeProperty).withClassName(soup.getClassName()).run();
    assertNotNull(soupPropertyBestBeforeStatus);
    assertTrue(soupPropertyBestBeforeStatus.getResult());
    Result<Boolean> soupPropertyPriceStatus = client.schema().propertyCreator()
      .withProperty(priceProperty).withClassName(soup.getClassName()).run();
    assertNotNull(soupPropertyPriceStatus);
    assertTrue(soupPropertyPriceStatus.getResult());
  }

  public void createWeaviateTestSchemaFoodWithReferenceProperty(WeaviateClient client) {
    createWeaviateTestSchemaFoodWithReferenceProperty(client, false);
  }

  public void createWeaviateTestSchemaFoodWithReferenceProperty(WeaviateClient client, boolean deprecatedMode) {
    createWeaviateTestSchemaFood(client, deprecatedMode);

    // reference property
    Property referenceProperty = Property.builder()
      .dataType(Arrays.asList("Pizza", "Soup"))
      .description("reference to other foods")
      .name("otherFoods")
      .build();
    Result<Boolean> pizzaRefAdd = client.schema().propertyCreator().withClassName("Pizza").withProperty(referenceProperty).run();
    assertNotNull(pizzaRefAdd);
    assertTrue(pizzaRefAdd.getResult());
    Result<Boolean> soupRefAdd = client.schema().propertyCreator().withClassName("Soup").withProperty(referenceProperty).run();
    assertNotNull(soupRefAdd);
    assertTrue(soupRefAdd.getResult());
  }

  public void createTestSchemaAndData(WeaviateClient client) {
    createTestSchemaAndData(client, false);
  }

  public void createTestSchemaAndData(WeaviateClient client, boolean deprecatedMode) {
    createWeaviateTestSchemaFood(client, deprecatedMode);

    // Create pizzas
    WeaviateObject[] menuPizza = new WeaviateObject[]{
      objectPizzaQuattroFormaggi(),
      objectPizzaFruttiDiMare(),
      objectPizzaHawaii(),
      objectPizzaDoener()
    };
    // Create soups
    WeaviateObject[] menuSoup = new WeaviateObject[]{
      objectSoupChicken(),
      objectSoupBeautiful()
    };
    Result<ObjectGetResponse[]> insertStatus = client.batch().objectsBatcher()
      .withObjects(menuPizza)
      .withObjects(menuSoup)
      .run();
    assertNotNull(insertStatus);
    assertNotNull(insertStatus.getResult());
    assertEquals(6, insertStatus.getResult().length);
  }

  public void createReplicatedTestSchemaAndData(WeaviateClient client) {
    createWeaviateReplicatedTestSchemaFood(client);

    // Create pizzas
    WeaviateObject[] menuPizza = new WeaviateObject[]{
      objectPizzaQuattroFormaggi(),
      objectPizzaFruttiDiMare(),
      objectPizzaHawaii(),
      objectPizzaDoener()
    };
    // Create soups
    WeaviateObject[] menuSoup = new WeaviateObject[]{
      objectSoupChicken(),
      objectSoupBeautiful()
    };
    Result<ObjectGetResponse[]> insertStatus = client.batch().objectsBatcher()
      .withObjects(menuPizza)
      .withObjects(menuSoup)
      .run();
    assertNotNull(insertStatus);
    assertNotNull(insertStatus.getResult());
    assertEquals(6, insertStatus.getResult().length);
  }

  public void createWeaviateReplicatedTestSchemaFood(WeaviateClient client) {
    // classes
    WeaviateClass pizza = WeaviateClass.builder()
      .className("Pizza")
      .description("A delicious religion like food and arguably the best export of Italy.")
      .invertedIndexConfig(InvertedIndexConfig.builder().indexTimestamps(true).build())
      .replicationConfig(ReplicationConfig.builder().factor(2).build())
      .build();
    WeaviateClass soup = WeaviateClass.builder()
      .className("Soup")
      .description("Mostly water based brew of sustenance for humans.")
      .replicationConfig(ReplicationConfig.builder().factor(2).build())
      .build();
    // create Pizza class
    Result<Boolean> pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run();
    assertNotNull(pizzaCreateStatus);
    assertTrue(pizzaCreateStatus.getResult());
    // create Soup class
    Result<Boolean> soupCreateStatus = client.schema().classCreator().withClass(soup).run();
    assertNotNull(soupCreateStatus);
    assertTrue(soupCreateStatus.getResult());
    // properties
    Property nameProperty = Property.builder()
      .dataType(Arrays.asList(DataType.STRING))
      .description("name")
      .name("name")
      .tokenization(Tokenization.FIELD)
      .build();
    Property descriptionProperty = Property.builder()
      .dataType(Arrays.asList(DataType.TEXT))
      .description("description")
      .name("description")
      .tokenization(Tokenization.WORD)
      .build();
    Property bestBeforeProperty = Property.builder()
      .dataType(Arrays.asList(DataType.DATE))
      .description("best before")
      .name("bestBefore")
      .build();
    Map<Object, Object> text2vecContextionary = new HashMap<>();
    text2vecContextionary.put("skip", true);
    Map<Object, Object> moduleConfig = new HashMap<>();
    moduleConfig.put("text2vec-contextionary", text2vecContextionary);
    Property priceProperty = Property.builder()
      .dataType(Arrays.asList(DataType.NUMBER))
      .description("price")
      .name("price")
      .moduleConfig(moduleConfig)
      .build();
    // Add name and description properties to Pizza
    Result<Boolean> pizzaPropertyNameStatus = client.schema().propertyCreator()
      .withProperty(nameProperty).withClassName(pizza.getClassName()).run();
    assertNotNull(pizzaPropertyNameStatus);
    assertTrue(pizzaPropertyNameStatus.getResult());
    Result<Boolean> pizzaPropertyDescriptionStatus = client.schema().propertyCreator()
      .withProperty(descriptionProperty).withClassName(pizza.getClassName()).run();
    assertNotNull(pizzaPropertyDescriptionStatus);
    assertTrue(pizzaPropertyDescriptionStatus.getResult());
    Result<Boolean> pizzaPropertyBestBeforeStatus = client.schema().propertyCreator()
      .withProperty(bestBeforeProperty).withClassName(pizza.getClassName()).run();
    assertNotNull(pizzaPropertyBestBeforeStatus);
    assertTrue(pizzaPropertyBestBeforeStatus.getResult());
    Result<Boolean> pizzaPropertyPriceStatus = client.schema().propertyCreator()
      .withProperty(priceProperty).withClassName(pizza.getClassName()).run();
    assertNotNull(pizzaPropertyPriceStatus);
    assertTrue(pizzaPropertyPriceStatus.getResult());
    // Add name and description properties to Soup
    Result<Boolean> soupPropertyNameStatus = client.schema().propertyCreator()
      .withProperty(nameProperty).withClassName(soup.getClassName()).run();
    assertNotNull(soupPropertyNameStatus);
    assertTrue(soupPropertyNameStatus.getResult());
    Result<Boolean> soupPropertyDescriptionStatus = client.schema().propertyCreator()
      .withProperty(descriptionProperty).withClassName(soup.getClassName()).run();
    assertNotNull(soupPropertyDescriptionStatus);
    assertTrue(soupPropertyDescriptionStatus.getResult());
    Result<Boolean> soupPropertyBestBeforeStatus = client.schema().propertyCreator()
      .withProperty(bestBeforeProperty).withClassName(soup.getClassName()).run();
    assertNotNull(soupPropertyBestBeforeStatus);
    assertTrue(soupPropertyBestBeforeStatus.getResult());
    Result<Boolean> soupPropertyPriceStatus = client.schema().propertyCreator()
      .withProperty(priceProperty).withClassName(soup.getClassName()).run();
    assertNotNull(soupPropertyPriceStatus);
    assertTrue(soupPropertyPriceStatus.getResult());
  }

  private WeaviateObject createObject(String id, String className, String name, String description, Float price, String bestBeforeRfc3339) {
    return WeaviateObject.builder()
      .id(id)
      .className(className)
      .properties(new HashMap<String, Object>() {{
        put("name", name);
        put("description", description);
        put("price", price);
        put("bestBefore", bestBeforeRfc3339);
      }}).build();
  }

  public void cleanupWeaviate(WeaviateClient client) {
    Result<Boolean> deleteAllStatus = client.schema().allDeleter().run();
    assertNotNull(deleteAllStatus);
    assertTrue(deleteAllStatus.getResult());
  }


  public void createSchemaPizza(WeaviateClient client) {
    createSchema(client, classPizza());
  }

  public void createSchemaSoup(WeaviateClient client) {
    createSchema(client, classSoup());
  }

  public void createSchemaFood(WeaviateClient client) {
    createSchemaPizza(client);
    createSchemaSoup(client);
  }

  public void createSchemaPizzaForTenants(WeaviateClient client) {
    createSchema(client, classPizzaForTenants());
  }

  public void createSchemaSoupForTenants(WeaviateClient client) {
    createSchema(client, classSoupForTenants());
  }

  public void createSchemaFoodForTenants(WeaviateClient client) {
    createSchemaPizzaForTenants(client);
    createSchemaSoupForTenants(client);
  }

  private void createSchema(WeaviateClient client, WeaviateClass cl) {
    Result<Boolean> createStatus = client.schema().classCreator().withClass(cl).run();
    assertThat(createStatus).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
  }


  public void createTenantsPizza(WeaviateClient client, Tenant... tenants) {
    createTenants(client, "Pizza", tenants);
  }

  public void createTenantsSoup(WeaviateClient client, Tenant... tenants) {
    createTenants(client, "Soup", tenants);
  }

  public void createTenantsFood(WeaviateClient client, Tenant... tenants) {
    createTenantsPizza(client, tenants);
    createTenantsSoup(client, tenants);
  }

  private void createTenants(WeaviateClient client, String className, Tenant[] tenants) {
    Result<Boolean> createStatus = client.schema().tenantsCreator()
      .withClassName(className)
      .withTenants(tenants)
      .run();
    assertThat(createStatus).isNotNull()
      .returns(false, Result::hasErrors)
      .returns(true, Result::getResult);
  }


  public void createDataPizza(WeaviateClient client) {
    createData(client, new WeaviateObject[]{
      objectPizzaQuattroFormaggi(),
      objectPizzaFruttiDiMare(),
      objectPizzaHawaii(),
      objectPizzaDoener(),
    });
  }

  public void createDataSoup(WeaviateClient client) {
    createData(client, new WeaviateObject[]{
      objectSoupChicken(),
      objectSoupBeautiful(),
    });
  }

  public void createDataFood(WeaviateClient client) {
    createData(client, new WeaviateObject[]{
      objectPizzaQuattroFormaggi(),
      objectPizzaFruttiDiMare(),
      objectPizzaHawaii(),
      objectPizzaDoener(),
      objectSoupChicken(),
      objectSoupBeautiful(),
    });
  }

  private void createData(WeaviateClient client, WeaviateObject[] objects) {
    Result<ObjectGetResponse[]> insertStatus = client.batch().objectsBatcher()
      .withObjects(objects)
      .run();

    assertThat(insertStatus).isNotNull()
      .returns(false, Result::hasErrors)
      .extracting(Result::getResult).asInstanceOf(ARRAY)
      .hasSize(objects.length);
  }

  public void createDataPizzaQuattroFormaggiForTenants(WeaviateClient client, String... tenants) {
    createDataForTenants(client, tenants, () -> new WeaviateObject[]{
      objectPizzaQuattroFormaggi(),
    });
  }

  public void createDataPizzaFruttiDiMareForTenants(WeaviateClient client, String... tenants) {
    createDataForTenants(client, tenants, () -> new WeaviateObject[]{
      objectPizzaFruttiDiMare(),
    });
  }

  public void createDataPizzaHawaiiForTenants(WeaviateClient client, String... tenants) {
    createDataForTenants(client, tenants, () -> new WeaviateObject[]{
      objectPizzaHawaii(),
    });
  }

  public void createDataPizzaDoenerForTenants(WeaviateClient client, String... tenants) {
    createDataForTenants(client, tenants, () -> new WeaviateObject[]{
      objectPizzaDoener(),
    });
  }

  public void createDataPizzaForTenants(WeaviateClient client, String... tenants) {
    createDataForTenants(client, tenants, () -> new WeaviateObject[]{
      objectPizzaQuattroFormaggi(),
      objectPizzaFruttiDiMare(),
      objectPizzaHawaii(),
      objectPizzaDoener(),
    });
  }

  public void createDataSoupForTenants(WeaviateClient client, String... tenants) {
    createDataForTenants(client, tenants, () -> new WeaviateObject[]{
      objectSoupChicken(),
      objectSoupBeautiful(),
    });
  }

  public void createDataFoodForTenants(WeaviateClient client, String... tenants) {
    createDataForTenants(client, tenants, () -> new WeaviateObject[]{
      objectPizzaQuattroFormaggi(),
      objectPizzaFruttiDiMare(),
      objectPizzaHawaii(),
      objectPizzaDoener(),
      objectSoupChicken(),
      objectSoupBeautiful(),
    });
  }

  private void createDataForTenants(WeaviateClient client, String[] tenants, Supplier<WeaviateObject[]> objectsSupplier) {
    WeaviateObject[] objects = Arrays.stream(tenants).flatMap(tenant ->
      Arrays.stream(objectsSupplier.get()).peek(obj -> obj.setTenant(tenant))
    ).toArray(WeaviateObject[]::new);

    createData(client, objects);
  }

  private WeaviateClass classPizza() {
    return classPizzaBuilder()
      .build();
  }

  private WeaviateClass classPizzaForTenants() {
    return classPizzaBuilder()
      .multiTenancyConfig(MultiTenancyConfig.builder()
        .enabled(true)
        .build())
      .build();
  }

  private WeaviateClass.WeaviateClassBuilder classPizzaBuilder() {
    return WeaviateClass.builder()
      .className("Pizza")
      .description("A delicious religion like food and arguably the best export of Italy.")
      .properties(classPropertiesFood())
      .invertedIndexConfig(InvertedIndexConfig.builder()
        .indexTimestamps(true)
        .build());
  }

  private WeaviateClass classSoup() {
    return classSoupBuilder()
      .build();
  }

  private WeaviateClass classSoupForTenants() {
    return classSoupBuilder()
      .multiTenancyConfig(MultiTenancyConfig.builder()
        .enabled(true)
        .build())
      .build();
  }

  private WeaviateClass.WeaviateClassBuilder classSoupBuilder() {
    return WeaviateClass.builder()
      .className("Soup")
      .description("Mostly water based brew of sustenance for humans.")
      .properties(classPropertiesFood());
  }

  private List<Property> classPropertiesFood() {
    Property nameProperty = Property.builder()
      .name("name")
      .description("property holding name")
      .dataType(Collections.singletonList(DataType.TEXT))
      .tokenization(Tokenization.FIELD)
      .build();
    Property descriptionProperty = Property.builder()
      .name("description")
      .description("property holding description")
      .dataType(Collections.singletonList(DataType.TEXT))
      .tokenization(Tokenization.WORD)
      .build();
    Property bestBeforeProperty = Property.builder()
      .name("bestBefore")
      .description("property holding best before")
      .dataType(Collections.singletonList(DataType.DATE))
      .build();
    Map<Object, Object> text2vecContextionary = new HashMap<>();
    text2vecContextionary.put("skip", true);
    Map<Object, Object> moduleConfig = new HashMap<>();
    moduleConfig.put("text2vec-contextionary", text2vecContextionary);
    Property priceProperty = Property.builder()
      .name("price")
      .description("property holding price")
      .dataType(Collections.singletonList(DataType.NUMBER))
      .moduleConfig(moduleConfig)
      .build();

    List<Property> properties = new ArrayList<>();
    properties.add(nameProperty);
    properties.add(descriptionProperty);
    properties.add(bestBeforeProperty);
    properties.add(priceProperty);

    return properties;
  }

  private WeaviateObject objectPizzaQuattroFormaggi() {
    return createObject(PIZZA_QUATTRO_FORMAGGI_ID, "Pizza", "Quattro Formaggi",
      "Pizza quattro formaggi Italian: [ˈkwattro forˈmaddʒi] (four cheese pizza) is a variety of pizza in Italian cuisine that is topped with a combination of four kinds of cheese, usually melted together, with (rossa, red) or without (bianca, white) tomato sauce. It is popular worldwide, including in Italy,[1] and is one of the iconic items from pizzerias's menus.",
      1.4f, "2022-01-02T03:04:05+01:00");
  }

  private WeaviateObject objectPizzaFruttiDiMare() {
    return createObject(PIZZA_FRUTTI_DI_MARE_ID, "Pizza", "Frutti di Mare",
      "Frutti di Mare is an Italian type of pizza that may be served with scampi, mussels or squid. It typically lacks cheese, with the seafood being served atop a tomato sauce.",
      2.5f, "2022-02-03T04:05:06+02:00");
  }

  private WeaviateObject objectPizzaHawaii() {
    return createObject(PIZZA_HAWAII_ID, "Pizza", "Hawaii",
      "Universally accepted to be the best pizza ever created.",
      1.1f, "2022-03-04T05:06:07+03:00");
  }

  private WeaviateObject objectPizzaDoener() {
    return createObject(PIZZA_DOENER_ID, "Pizza", "Doener",
      "A innovation, some say revolution, in the pizza industry.",
      1.2f, "2022-04-05T06:07:08+04:00");
  }

  private WeaviateObject objectSoupChicken() {
    return createObject(SOUP_CHICKENSOUP_ID, "Soup", "ChickenSoup",
      "Used by humans when their inferior genetics are attacked by microscopic organisms.",
      2.0f, "2022-05-06T07:08:09+05:00");
  }

  private WeaviateObject objectSoupBeautiful() {
    return createObject(SOUP_BEAUTIFUL_ID, "Soup", "Beautiful",
      "Putting the game of letter soups to a whole new level.",
      3f, "2022-06-07T08:09:10+06:00");
  }


  public static class DocumentPassageSchema {

    public final String DOCUMENT = "Document";
    public final String[] DOCUMENT_IDS = new String[]{
      "00000000-0000-0000-0000-00000000000a",
      "00000000-0000-0000-0000-00000000000b",
      "00000000-0000-0000-0000-00000000000c",
      "00000000-0000-0000-0000-00000000000d",
    };
    public final String PASSAGE = "Passage";
    public final String[] PASSAGE_IDS = new String[]{
      "00000000-0000-0000-0000-000000000001",
      "00000000-0000-0000-0000-000000000002",
      "00000000-0000-0000-0000-000000000003",
      "00000000-0000-0000-0000-000000000004",
      "00000000-0000-0000-0000-000000000005",
      "00000000-0000-0000-0000-000000000006",
      "00000000-0000-0000-0000-000000000007",
      "00000000-0000-0000-0000-000000000008",
      "00000000-0000-0000-0000-000000000009",
      "00000000-0000-0000-0000-000000000010",
      "00000000-0000-0000-0000-000000000011",
      "00000000-0000-0000-0000-000000000012",
      "00000000-0000-0000-0000-000000000013",
      "00000000-0000-0000-0000-000000000014",
      "00000000-0000-0000-0000-000000000015",
      "00000000-0000-0000-0000-000000000016",
      "00000000-0000-0000-0000-000000000017",
      "00000000-0000-0000-0000-000000000018",
      "00000000-0000-0000-0000-000000000019",
      "00000000-0000-0000-0000-000000000020"
    };

    private void createDocumentClass(WeaviateClient client) {
      Property titleProperty = Property.builder()
        .dataType(Collections.singletonList(DataType.TEXT))
        .name("title")
        .tokenization(Tokenization.FIELD)
        .build();
      WeaviateClass document = WeaviateClass.builder()
        .className(DOCUMENT)
        .properties(Collections.singletonList(titleProperty))
        .invertedIndexConfig(InvertedIndexConfig.builder().indexTimestamps(true).build())
        .build();
      Result<Boolean> documentCreateStatus = client.schema().classCreator().withClass(document).run();
      assertNotNull(documentCreateStatus);
      assertTrue(documentCreateStatus.getResult());
    }

    private void createPassageClass(WeaviateClient client) {
      Property contentProperty = Property.builder()
        .dataType(Collections.singletonList(DataType.TEXT))
        .name("content")
        .tokenization(Tokenization.FIELD)
        .build();
      Property typeProperty = Property.builder()
        .dataType(Collections.singletonList(DataType.TEXT))
        .name("type")
        .tokenization(Tokenization.FIELD)
        .build();
      Property ofDocumentProperty = Property.builder()
        .dataType(Collections.singletonList(DOCUMENT))
        .name("ofDocument")
        .build();
      WeaviateClass document = WeaviateClass.builder()
        .className(PASSAGE)
        .properties(Arrays.asList(contentProperty, typeProperty, ofDocumentProperty))
        .invertedIndexConfig(InvertedIndexConfig.builder().indexTimestamps(true).build())
        .build();
      Result<Boolean> documentCreateStatus = client.schema().classCreator().withClass(document).run();
      assertNotNull(documentCreateStatus);
      assertTrue(documentCreateStatus.getResult());
    }

    private void insertData(WeaviateClient client) {
      WeaviateObject[] documents = new WeaviateObject[DOCUMENT_IDS.length];
      for (int i = 0; i < DOCUMENT_IDS.length; i++) {
        String title = String.format("Title of the document %s", i);
        WeaviateObject document = WeaviateObject.builder()
          .id(DOCUMENT_IDS[i])
          .className(DOCUMENT)
          .properties(new HashMap<String, Object>() {{
            put("title", title);
          }}).build();
        documents[i] = document;
      }
      WeaviateObject[] passages = new WeaviateObject[PASSAGE_IDS.length];
      for (int i = 0; i < PASSAGE_IDS.length; i++) {
        String content = String.format("Passage content %s", i);
        WeaviateObject passage = WeaviateObject.builder()
          .id(PASSAGE_IDS[i])
          .className(PASSAGE)
          .properties(new HashMap<String, Object>() {{
            put("content", content);
            put("type", "document-passage");
          }}).build();
        passages[i] = passage;
      }
      Result<ObjectGetResponse[]> insertStatus = client.batch().objectsBatcher()
        .withObjects(documents)
        .withObjects(passages)
        .run();
      assertNotNull(insertStatus);
      assertNull(insertStatus.getError());
      assertNotNull(insertStatus.getResult());
      // first 10 passages assign to document 1
      createReferences(client, documents[0], Arrays.copyOfRange(passages, 0, 10));
      // next 4 passages assign to document 2
      createReferences(client, documents[1], Arrays.copyOfRange(passages, 10, 14));
    }

    private void createReferences(WeaviateClient client, WeaviateObject document, WeaviateObject[] passages) {
      SingleRef ref = client.data().referencePayloadBuilder()
        .withID(document.getId()).withClassName(DOCUMENT).payload();
      for (WeaviateObject passage : passages) {
        Result<Boolean> createOfDocumentRef = client.data().referenceCreator()
          .withID(passage.getId())
          .withClassName(PASSAGE)
          .withReferenceProperty("ofDocument")
          .withReference(ref)
          .run();
        assertNotNull(createOfDocumentRef);
        assertNull(createOfDocumentRef.getError());
        assertTrue(createOfDocumentRef.getResult());
      }
    }

    public void createSchema(WeaviateClient client) {
      createDocumentClass(client);
      createPassageClass(client);
    }

    public void createAndInsertData(WeaviateClient client) {
      createSchema(client);
      insertData(client);
    }

    public void cleanupWeaviate(WeaviateClient client) {
      Result<Boolean> deleteAllStatus = client.schema().allDeleter().run();
      assertNotNull(deleteAllStatus);
      assertTrue(deleteAllStatus.getResult());
    }
  }

  public DocumentPassageSchema documentPassageSchema() {
    return new DocumentPassageSchema();
  }
}
