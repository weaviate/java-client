package io.weaviate.client.v1.schema.model;

import com.google.gson.GsonBuilder;
import java.util.Arrays;
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
            .dataType(Arrays.asList(DataType.NUMBER))
            .description("price")
            .name("price")
            .moduleConfig(moduleConfig)
            .indexFilterable(true)
            .indexSearchable(true)
            .build();

    String expected = "{\"name\":\"price\",\"dataType\":[\"number\"],\"description\":\"price\"," +
            "\"moduleConfig\":{\"text2vec-contextionary\":{\"vectorizePropertyName\":false}}," +
            "\"indexFilterable\":true,\"indexSearchable\":true}";
    // when
    String result = new GsonBuilder().create().toJson(priceProperty);
    // then
    assertNotNull(result);
    assertEquals(expected, result);
  }
}
