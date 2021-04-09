package technology.semi.weaviate.integration.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;
import org.junit.Assert;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.v1.batch.api.ObjectsBatcher;
import technology.semi.weaviate.client.v1.batch.model.ObjectGetResponse;
import technology.semi.weaviate.client.v1.data.model.Object;
import technology.semi.weaviate.client.v1.schema.model.Class;
import technology.semi.weaviate.client.v1.schema.model.DataType;
import technology.semi.weaviate.client.v1.schema.model.Property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WeaviateTestGenerics {
  public void createWeaviateTestSchemaFood(WeaviateClient client) {
    // classes
    Class pizza = Class.builder()
            .className("Pizza")
            .description("A delicious religion like food and arguably the best export of Italy.")
            .build();
    Class soup = Class.builder()
            .className("Soup")
            .description("Mostly water based brew of sustenance for humans.")
            .build();
    // create Pizza class
    Boolean pizzaCreateStatus = client.schema().classCreator().withClass(pizza).run();
    assertTrue(pizzaCreateStatus);
    // create Soup class
    Boolean soupCreateStatus = client.schema().classCreator().withClass(soup).run();
    assertTrue(soupCreateStatus);
    // properties
    Property nameProperty = Property.builder()
            .dataType(Arrays.asList(DataType.STRING))
            .description("name")
            .name("name")
            .build();
    Property descriptionProperty = Property.builder()
            .dataType(Arrays.asList(DataType.TEXT))
            .description("description")
            .name("description")
            .build();
    // Add name and description properties to Pizza
    Boolean pizzaPropertyNameStatus = client.schema().propertyCreator()
            .withProperty(nameProperty).withClassName(pizza.getClassName()).run();
    assertTrue(pizzaPropertyNameStatus);
    Boolean pizzaPropertyDescritpionStatus = client.schema().propertyCreator()
            .withProperty(descriptionProperty).withClassName(pizza.getClassName()).run();
    assertTrue(pizzaPropertyDescritpionStatus);
    // Add name and description properties to Soup
    Boolean soupPropertyNameStatus = client.schema().propertyCreator()
            .withProperty(nameProperty).withClassName(soup.getClassName()).run();
    assertTrue(soupPropertyNameStatus);
    Boolean soupPropertyDescritpionStatus = client.schema().propertyCreator()
            .withProperty(descriptionProperty).withClassName(soup.getClassName()).run();
    assertTrue(soupPropertyDescritpionStatus);
  }

  public void createWeaviateTestSchemaFoodWithReferenceProperty(WeaviateClient client) {
    createWeaviateTestSchemaFood(client);
    // reference property
    Property referenceProperty = Property.builder()
            .dataType(Arrays.asList("Pizza", "Soup"))
            .description("reference to other foods")
            .name("otherFoods")
            .build();
    Boolean pizzaRefAdd = client.schema().propertyCreator().withClassName("Pizza").withProperty(referenceProperty).run();
    assertTrue(pizzaRefAdd);
    Boolean soupRefAdd = client.schema().propertyCreator().withClassName("Soup").withProperty(referenceProperty).run();
    assertTrue(soupRefAdd);
  }

  public void createTestSchemaAndData(WeaviateClient client) {
    createWeaviateTestSchemaFood(client);
    // Create pizzas
    Object[] menuPizza = new Object[]{
            createObject("Pizza", "Quattro Formaggi", "Pizza quattro formaggi Italian: [ˈkwattro forˈmaddʒi] (four cheese pizza) is a variety of pizza in Italian cuisine that is topped with a combination of four kinds of cheese, usually melted together, with (rossa, red) or without (bianca, white) tomato sauce. It is popular worldwide, including in Italy,[1] and is one of the iconic items from pizzerias's menus."),
            createObject("Pizza", "Frutti di Mare", "Frutti di Mare is an Italian type of pizza that may be served with scampi, mussels or squid. It typically lacks cheese, with the seafood being served atop a tomato sauce."),
            createObject("Pizza", "Hawaii", "Universally accepted to be the best pizza ever created."),
            createObject("Pizza", "Doener", "A innovation, some say revolution, in the pizza industry."),
    };
    // Create soups
    Object[] menuSoup = new Object[]{
            createObject("Soup", "ChickenSoup", "Used by humans when their inferior genetics are attacked by microscopic organisms."),
            createObject("Soup", "Beautiful", "Putting the game of letter soups to a whole new level."),
    };
    ObjectsBatcher objectsBatcher = client.batch().objectsBatcher();
    Stream.of(menuPizza).forEach(objectsBatcher::withObject);
    Stream.of(menuSoup).forEach(objectsBatcher::withObject);
    ObjectGetResponse[] insertStatus = objectsBatcher.run();
    assertNotNull(insertStatus);
    assertEquals(6, insertStatus.length);
  }

  private Object createObject(String className, String name, String description) {
    return Object.builder().className(className).properties(new HashMap<String, java.lang.Object>() {{
              put("name", name);
              put("description", description);
            }}).build();
  }

  public void cleanupWeaviate(WeaviateClient client) {
    Boolean deleteAllStatus = client.schema().allDeleter().run();
    assertTrue(deleteAllStatus);
  }
}
