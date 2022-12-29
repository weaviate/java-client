package technology.semi.weaviate.client.v1.schema.model;

import com.google.gson.GsonBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.junit.Test;

public class PropertyTest extends TestCase {

  @Test
  public void testSerialize() {
    // given
    Map<Object, Object> text2vecContextionary = new HashMap<>();
    text2vecContextionary.put("vectorizePropertyName", false);
    Map<Object, Object> moduleConfig = new HashMap<>();
    moduleConfig.put("text2vec-contextionary", text2vecContextionary);
    Property priceProperty = Property.builder()
      .dataType(Collections.singletonList(DataType.NUMBER))
      .description("price")
      .name("price")
      .moduleConfig(moduleConfig)
      .build();
    String expected = "{\"name\":\"price\",\"dataType\":[\"number\"],\"description\":\"price\"," +
      "\"moduleConfig\":{\"text2vec-contextionary\":{\"vectorizePropertyName\":false}}}";
    // when
    String result = new GsonBuilder().create().toJson(priceProperty);
    // then
    assertNotNull(result);
    assertEquals(expected, result);
  }
}
