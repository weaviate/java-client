package technology.semi.weaviate.integration.client;

import java.util.Arrays;
import org.junit.Assert;
import technology.semi.weaviate.client.WeaviateClient;
import technology.semi.weaviate.client.v1.schema.model.Class;
import technology.semi.weaviate.client.v1.schema.model.DataType;
import technology.semi.weaviate.client.v1.schema.model.Property;

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
    Assert.assertTrue(pizzaCreateStatus);
    // create Soup class
    Boolean soupCreateStatus = client.schema().classCreator().withClass(soup).run();
    Assert.assertTrue(soupCreateStatus);
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
    Assert.assertTrue(pizzaPropertyNameStatus);
    Boolean pizzaPropertyDescritpionStatus = client.schema().propertyCreator()
            .withProperty(descriptionProperty).withClassName(pizza.getClassName()).run();
    Assert.assertTrue(pizzaPropertyDescritpionStatus);
    // Add name and description properties to Soup
    Boolean soupPropertyNameStatus = client.schema().propertyCreator()
            .withProperty(nameProperty).withClassName(soup.getClassName()).run();
    Assert.assertTrue(soupPropertyNameStatus);
    Boolean soupPropertyDescritpionStatus = client.schema().propertyCreator()
            .withProperty(descriptionProperty).withClassName(soup.getClassName()).run();
    Assert.assertTrue(soupPropertyDescritpionStatus);
  }

  public void cleanupWeaviate(WeaviateClient client) {
    Boolean deleteAllStatus = client.schema().allDeleter().run();
    Assert.assertTrue(deleteAllStatus);
  }
}
