package technology.semi.weaviate.client.v1.data.model;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class ObjectsListResponseTest extends TestCase {
  @Test
  public void testDeserialization() throws FileNotFoundException {
    // given
    File jsonFile = new File("src/test/resources/json/objects-list-response.json");
    InputStreamReader reader = new InputStreamReader(new FileInputStream(jsonFile));
    // when
    ObjectsListResponse resp = new Gson().fromJson(reader, ObjectsListResponse.class);
    // then
    Assert.assertNotNull(resp);
    Assert.assertNotNull(resp.getObjects());
    Assert.assertEquals(4, resp.getObjects().length);
    Assert.assertEquals(4, resp.getTotalResults());
  }
}