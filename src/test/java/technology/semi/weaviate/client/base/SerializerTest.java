package technology.semi.weaviate.client.base;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class SerializerTest extends TestCase {

  @Test
  public void testToResponse() {
    // given
    Serializer s = new Serializer();
    String description = "test äüëö";
    String jsonString = "{\"description\":\""+description+"\"}";
    // when
    TestObj deserialized = s.toResponse(jsonString, TestObj.class);
    // then
    Assert.assertNotNull(deserialized);
    Assert.assertEquals(description, deserialized.getDescription());
  }

  @Test
  public void testToJsonString() {
    // given
    Serializer s = new Serializer();
    TestObj obj = new TestObj("test äüëö");
    // when
    String serialized = s.toJsonString(obj);
    // then
    Assert.assertNotNull(serialized);
    Assert.assertEquals("{\"description\":\"test äüëö\"}", serialized);
  }
}

class TestObj {
  private String description;
  public TestObj(String description) {
    this.description = description;
  }
  public String getDescription() {
    return description;
  }
}