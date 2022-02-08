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

  @Test
  public void testErrorResponse() {
    // given
    Serializer s = new Serializer();
    String jsonString = "{\"error\":[{\"message\":\"get extend: unknown capability: featureProjection\"}]}";
    // when
    WeaviateErrorResponse deserialized = s.toResponse(jsonString, WeaviateErrorResponse.class);
    // then
    Assert.assertNotNull(deserialized);
    Assert.assertNull(deserialized.getMessage());
    Assert.assertNull(deserialized.getCode());
    Assert.assertNotNull(deserialized.getError());
    Assert.assertNotNull(deserialized.getError().get(0));
    Assert.assertEquals("get extend: unknown capability: featureProjection", deserialized.getError().get(0).getMessage());
  }

  @Test
  public void testErrorResponseWithNoError() {
    // given
    Serializer s = new Serializer();
    String jsonString = "{\"code\":601,\"message\":\"id in body must be of type uuid: \\\"TODO_4\\\"\"}";
    // when
    WeaviateErrorResponse deserialized = s.toResponse(jsonString, WeaviateErrorResponse.class);
    // then
    Assert.assertNotNull(deserialized);
    Assert.assertNull(deserialized.getError());
    Assert.assertEquals(new Integer(601), deserialized.getCode());
    Assert.assertEquals("id in body must be of type uuid: \"TODO_4\"", deserialized.getMessage());
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