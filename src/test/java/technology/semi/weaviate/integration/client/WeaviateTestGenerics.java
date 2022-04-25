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

import static org.junit.Assert.*;

public class WeaviateTestGenerics {
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
            createObject("Pizza", "Quattro Formaggi", "Pizza quattro formaggi Italian: [ˈkwattro forˈmaddʒi] (four cheese pizza) is a variety of pizza in Italian cuisine that is topped with a combination of four kinds of cheese, usually melted together, with (rossa, red) or without (bianca, white) tomato sauce. It is popular worldwide, including in Italy,[1] and is one of the iconic items from pizzerias's menus.", 1.4f),
            createObject("Pizza", "Frutti di Mare", "Frutti di Mare is an Italian type of pizza that may be served with scampi, mussels or squid. It typically lacks cheese, with the seafood being served atop a tomato sauce.", 2.5f),
            createObject("Pizza", "Hawaii", "Universally accepted to be the best pizza ever created.",1.1f),
            createObject("Pizza", "Doener", "A innovation, some say revolution, in the pizza industry.", 1.2f),
    };
    // Create soups
    WeaviateObject[] menuSoup = new WeaviateObject[]{
            createObject("Soup", "ChickenSoup", "Used by humans when their inferior genetics are attacked by microscopic organisms.", 2.0f),
            createObject("Soup", "Beautiful", "Putting the game of letter soups to a whole new level.", 3f),
    };
    ObjectsBatcher objectsBatcher = client.batch().objectsBatcher();
    Stream.of(menuPizza).forEach(objectsBatcher::withObject);
    Stream.of(menuSoup).forEach(objectsBatcher::withObject);
    Result<ObjectGetResponse[]> insertStatus = objectsBatcher.run();
    assertNotNull(insertStatus);
    assertNotNull(insertStatus.getResult());
    assertEquals(6, insertStatus.getResult().length);
  }

  private WeaviateObject createObject(String className, String name, String description, Float price) {
    return WeaviateObject.builder().className(className).properties(new HashMap<String, Object>() {{
              put("name", name);
              put("description", description);
              put("price", price);
            }}).build();
  }

  public void cleanupWeaviate(WeaviateClient client) {
    Result<Boolean> deleteAllStatus = client.schema().allDeleter().run();
    assertNotNull(deleteAllStatus);
    assertTrue(deleteAllStatus.getResult());
  }
}
