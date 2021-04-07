package technology.semi.weaviate.client.v1.data.model;

import com.google.gson.Gson;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class ObjectsResponseTest extends TestCase {
  String json = "{\"deprecations\":null,\"objects\":[{\"class\":\"Pizza\",\"creationTimeUnix\":1617828214945," +
          "\"id\":\"302ae446-435c-471e-a434-f84bfb10b2b0\",\"lastUpdateTimeUnix\":1617828214945,\"properties\":{\"description\":\"meat\"," +
          "\"name\":\"Pepperoni\"},\"vectorWeights\":null},{\"class\":\"Pizza\",\"creationTimeUnix\":1617828214916," +
          "\"id\":\"5314ade4-61b6-48a8-b4b9-a89310693a63\",\"lastUpdateTimeUnix\":1617828214916,\"properties\":{\"description\":\"plain\"," +
          "\"name\":\"Margherita\"},\"vectorWeights\":null},{\"class\":\"Soup\",\"creationTimeUnix\":1617828215003," +
          "\"id\":\"2b6de69f-4eb7-45df-8a0a-2bb951bb0d0e\",\"lastUpdateTimeUnix\":1617828215003,\"properties\":{\"description\":\"vegetarian\"," +
          "\"name\":\"Tofu\"},\"vectorWeights\":null},{\"class\":\"Soup\",\"creationTimeUnix\":1617828214974," +
          "\"id\":\"7f2d3d76-1a74-4563-bd4e-18f5f50facdf\",\"lastUpdateTimeUnix\":1617828214974,\"properties\":{\"description\":\"plain\"," +
          "\"name\":\"Chicken\"},\"vectorWeights\":null}],\"totalResults\":4}";

  @Test
  public void testDeserialization() {
    // given
    // when
    ObjectsResponse resp = new Gson().fromJson(json, ObjectsResponse.class);
    // then
    Assert.assertNotNull(resp);
    Assert.assertNotNull(resp.getObjects());
    Assert.assertEquals(4, resp.getObjects().length);
    Assert.assertEquals(4, resp.getTotalResults());
  }

  @Test
  public void testDeserializationInputStream() {
    // given
    InputStream inputStream = new ByteArrayInputStream(json.getBytes());
    // when
    ObjectsResponse resp = new Gson().fromJson(new InputStreamReader(inputStream), ObjectsResponse.class);
    // then
    Assert.assertNotNull(resp);
    Assert.assertNotNull(resp.getObjects());
    Assert.assertEquals(4, resp.getObjects().length);
    Assert.assertEquals(4, resp.getTotalResults());
  }
}