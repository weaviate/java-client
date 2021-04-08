package technology.semi.weaviate.client.v1.contextionary.model;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class C11yWordsResponseTest extends TestCase {
  @Test
  public void testDeserialization() throws FileNotFoundException {
    // given
    File jsonFile = new File("src/test/resources/json/c11y-concepts.json");
    InputStreamReader reader = new InputStreamReader(new FileInputStream(jsonFile));
    // when
    C11yWordsResponse resp = new Gson().fromJson(new BufferedReader(reader), C11yWordsResponse.class);
    // then
    Assert.assertNotNull(resp);
    Assert.assertEquals("pizzaHawaii", resp.getConcatenatedWord().getConcatenatedWord());
  }
}