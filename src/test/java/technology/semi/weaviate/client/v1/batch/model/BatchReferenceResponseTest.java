package technology.semi.weaviate.client.v1.batch.model;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class BatchReferenceResponseTest extends TestCase {
  @Test
  public void testDeserialization() throws FileNotFoundException {
    // given
    File jsonFile = new File("src/test/resources/json/batch-reference-response.json");
    InputStreamReader reader = new InputStreamReader(new FileInputStream(jsonFile));
    // when
    BatchReferenceResponse[] resp = new Gson().fromJson(new BufferedReader(reader), BatchReferenceResponse[].class);
    // then
    Assert.assertNotNull(resp);
    Assert.assertEquals(4, resp.length);
    Assert.assertEquals("weaviate://localhost/Pizza/97fa5147-bdad-4d74-9a81-f8babc811b09/otherFoods", resp[0].getFrom());
    Assert.assertEquals("weaviate://localhost/07473b34-0ab2-4120-882d-303d9e13f7af", resp[0].getTo());
    Assert.assertEquals("SUCCESS", resp[0].getResult().getStatus());
  }
}