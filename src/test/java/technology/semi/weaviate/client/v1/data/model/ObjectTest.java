package technology.semi.weaviate.client.v1.data.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class ObjectTest extends TestCase {
  @Test
  public void testSerializeObject() {
    // given
    Map<String, java.lang.Object> properties = new HashMap<>();
    properties.put("name", "Pizza");
    properties.put("description", "Italian pizzas");
    Object obj = Object.builder()
            .id("uuid")
            .className("class")
            .creationTimeUnix(1000l)
            .lastUpdateTimeUnix(2000l)
            .vector(new Float[]{ 1.0f, 2.0f })
            .properties(properties)
            .build();
    // when
    String result = new GsonBuilder().setPrettyPrinting().create().toJson(obj);
    // then
    Assert.assertNotNull(result);
  }

  @Test
  public void testDeserialization() throws FileNotFoundException {
    // given
    File jsonFile = new File("src/test/resources/json/object.json");
    InputStreamReader reader = new InputStreamReader(new FileInputStream(jsonFile));
    // when
    Object result = new Gson().fromJson(reader, Object.class);
    // then
    Assert.assertNotNull(result);
    Assert.assertNotNull(result.getId());
    Assert.assertNotNull(result.getProperties());
    Assert.assertNotNull(result.getProperties().get("name"));
    Assert.assertNotNull(result.getProperties().get("description"));
    Assert.assertNotNull(result.getAdditional());
    Assert.assertEquals(2, result.getAdditional().size());
    Assert.assertNull(result.getAdditional().get("classification"));
    Assert.assertNotNull(result.getAdditional().get("nearestNeighbors"));
    Assert.assertNotNull(result.getVector());
  }

  @Test
  public void testDeserialization2() throws FileNotFoundException {
    // given
    File jsonFile = new File("src/test/resources/json/object2.json");
    InputStreamReader reader = new InputStreamReader(new FileInputStream(jsonFile));
    // when
    Object result = new Gson().fromJson(reader, Object.class);
    // then
    Assert.assertNotNull(result);
    Assert.assertNotNull(result.getId());
    Assert.assertNotNull(result.getProperties());
    Assert.assertNotNull(result.getProperties().get("name"));
    Assert.assertNotNull(result.getProperties().get("description"));
    Assert.assertNotNull(result.getProperties().get("otherFoods"));
    Assert.assertTrue(result.getProperties().get("otherFoods") instanceof List);
    List otherFoods = (List) result.getProperties().get("otherFoods");
    Assert.assertTrue(otherFoods.get(0) instanceof Map);
    Map otherFood0 = (Map) otherFoods.get(0);
    Map otherFood1 = (Map) otherFoods.get(1);
    Assert.assertEquals("weaviate://localhost/97fa5147-bdad-4d74-9a81-f8babc811b09", otherFood0.get("beacon"));
    Assert.assertEquals("/v1/objects/97fa5147-bdad-4d74-9a81-f8babc811b09", otherFood0.get("href"));
    Assert.assertEquals("weaviate://localhost/07473b34-0ab2-4120-882d-303d9e13f7af", otherFood1.get("beacon"));
    Assert.assertEquals("/v1/objects/07473b34-0ab2-4120-882d-303d9e13f7af", otherFood1.get("href"));
  }
}