package technology.semi.weaviate.integration.client;

import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.base.Result;
import technology.semi.weaviate.client.v1.batch.api.ObjectsBatcher;
import technology.semi.weaviate.client.v1.batch.model.ObjectGetResponse;
import technology.semi.weaviate.client.v1.data.model.WeaviateObject;
import technology.semi.weaviate.client.v1.misc.model.InvertedIndexConfig;
import technology.semi.weaviate.client.v1.schema.model.DataType;
import technology.semi.weaviate.client.v1.schema.model.Property;
import technology.semi.weaviate.client.v1.schema.model.Tokenization;
import technology.semi.weaviate.client.v1.schema.model.WeaviateClass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WeaviateTestGenerics {

  public static final String PIZZA_QUATTRO_FORMAGGI_ID = "10523cdd-15a2-42f4-81fa-267fe92f7cd6";
  public static final String PIZZA_FRUTTI_DI_MARE_ID = "927dd3ac-e012-4093-8007-7799cc7e81e4";
  public static final String PIZZA_HAWAII_ID = "f824a18e-c430-4475-9bef-847673fbb54e";
  public static final String PIZZA_DOENER_ID = "d2b393ff-4b26-48c7-b554-218d970a9e17";
  public static final String SOUP_CHICKENSOUP_ID = "8c156d37-81aa-4ce9-a811-621e2702b825";
  public static final String SOUP_BEAUTIFUL_ID = "27351361-2898-4d1a-aad7-1ca48253eb0b";


  public void createWeaviateTestSchemaFood(WeaviateClient client) {
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

  public void createWeaviateTestSchemaFoodWithReferenceProperty(WeaviateClient client) {
    createWeaviateTestSchemaFood(client);
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
    createWeaviateTestSchemaFood(client);


    // Create pizzas
    WeaviateObject[] menuPizza = new WeaviateObject[]{
            createObject(PIZZA_QUATTRO_FORMAGGI_ID, "Pizza", "Quattro Formaggi",
                    "Pizza quattro formaggi Italian: [ˈkwattro forˈmaddʒi] (four cheese pizza) is a variety of pizza in Italian cuisine that is topped with a combination of four kinds of cheese, usually melted together, with (rossa, red) or without (bianca, white) tomato sauce. It is popular worldwide, including in Italy,[1] and is one of the iconic items from pizzerias's menus.",
                    1.4f, "2022-01-02T03:04:05+01:00"),
            createObject(PIZZA_FRUTTI_DI_MARE_ID, "Pizza", "Frutti di Mare",
                    "Frutti di Mare is an Italian type of pizza that may be served with scampi, mussels or squid. It typically lacks cheese, with the seafood being served atop a tomato sauce.",
                    2.5f, "2022-02-03T04:05:06+02:00"),
            createObject(PIZZA_HAWAII_ID, "Pizza", "Hawaii",
                    "Universally accepted to be the best pizza ever created.",
                    1.1f, "2022-03-04T05:06:07+03:00"),
            createObject(PIZZA_DOENER_ID, "Pizza", "Doener",
                    "A innovation, some say revolution, in the pizza industry.",
                    1.2f, "2022-04-05T06:07:08+04:00"),
    };
    // Create soups
    WeaviateObject[] menuSoup = new WeaviateObject[]{
            createObject(SOUP_CHICKENSOUP_ID, "Soup", "ChickenSoup",
                    "Used by humans when their inferior genetics are attacked by microscopic organisms.",
                    2.0f, "2022-05-06T07:08:09+05:00"),
            createObject(SOUP_BEAUTIFUL_ID, "Soup", "Beautiful",
                    "Putting the game of letter soups to a whole new level.",
                    3f, "2022-06-07T08:09:10+06:00"),
    };
    ObjectsBatcher objectsBatcher = client.batch().objectsBatcher();
    Stream.of(menuPizza).forEach(objectsBatcher::withObject);
    Stream.of(menuSoup).forEach(objectsBatcher::withObject);
    Result<ObjectGetResponse[]> insertStatus = objectsBatcher.run();
    assertNotNull(insertStatus);
    assertNotNull(insertStatus.getResult());
    assertEquals(6, insertStatus.getResult().length);
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
}
