package technology.semi.weaviate.client.v1.data.util;

import junit.framework.TestCase;
import org.junit.Test;

public class ObjectsPathBuilderTest extends TestCase {

  @Test
  public void testPathBuilder() {
    // given
    Integer limit = 20;
    String[] additional = new String[]{ "vector", "fp" };
    // when
    String path = ObjectsPathBuilder.builder()
      .build().buildPath();
    String pathLimit = ObjectsPathBuilder.builder()
      .limit(limit)
      .build().buildPath();
    String pathAdditional = ObjectsPathBuilder.builder()
      .additional(additional)
      .build().buildPath();
    String pathLimitAndAdditional = ObjectsPathBuilder.builder()
      .limit(limit).additional(additional)
      .build().buildPath();
    // then
    assertEquals("/objects", path);
    assertEquals("/objects?limit=20", pathLimit);
    assertEquals("/objects?include=vector,fp", pathAdditional);
    assertEquals("/objects?include=vector,fp&limit=20", pathLimitAndAdditional);
  }

  @Test
  public void testPathBuilderWithId() {
    // given
    String id = "some-uuid";
    Integer limit = 20;
    String[] additional = new String[]{ "vector", "fp" };
    // when
    String pathId = ObjectsPathBuilder.builder()
      .id(id)
      .build().buildPath();
    String pathIdAndLimit = ObjectsPathBuilder.builder()
      .id(id).limit(limit)
      .build().buildPath();
    String pathIdAndAdditional = ObjectsPathBuilder.builder()
      .id(id).additional(additional)
      .build().buildPath();
    String pathIdAndLimitAndAdditional = ObjectsPathBuilder.builder()
      .id(id).limit(limit).additional(additional)
      .build().buildPath();
    // then
    assertEquals("/objects/some-uuid", pathId);
    assertEquals("/objects/some-uuid?limit=20", pathIdAndLimit);
    assertEquals("/objects/some-uuid?include=vector,fp", pathIdAndAdditional);
    assertEquals("/objects/some-uuid?include=vector,fp&limit=20", pathIdAndLimitAndAdditional);
  }

  @Test
  public void testPathBuilderWithClassName() {
    // given
    String version = "1.18.0";
    String id = "some-uuid";
    String className = "some-class";
    Integer limit = 20;
    String[] additional = new String[]{ "vector", "fp" };
    // when
    String pathIdAndClassName = ObjectsPathBuilder.builder()
      .id(id).className(className)
      .build().buildPath(version);
    String pathIdAndClassNameAndLimit = ObjectsPathBuilder.builder()
      .id(id).className(className).limit(limit)
      .build().buildPath(version);
    String pathIdAndClassNameAndAdditional = ObjectsPathBuilder.builder()
      .id(id).className(className).additional(additional)
      .build().buildPath(version);
    String pathIdAndClassNameAndLimitAndAdditional = ObjectsPathBuilder.builder()
      .id(id).className(className).limit(limit).additional(additional)
      .build().buildPath(version);
    // then
    assertEquals("/objects/some-class/some-uuid", pathIdAndClassName);
    assertEquals("/objects/some-class/some-uuid?include=vector,fp", pathIdAndClassNameAndAdditional);
    assertEquals("/objects/some-class/some-uuid?limit=20", pathIdAndClassNameAndLimit);
    assertEquals("/objects/some-class/some-uuid?include=vector,fp&limit=20", pathIdAndClassNameAndLimitAndAdditional);
  }

  @Test
  public void testPathBuilderWithClassNameAndVersionEarlierThen1_14() {
    // given
    String version = "1.13.2";
    String id = "some-uuid";
    String className = "some-class";
    Integer limit = 20;
    String[] additional = new String[]{ "vector", "fp" };
    // when
    String pathIdAndClassName = ObjectsPathBuilder.builder()
      .id(id).className(className)
      .build().buildPath(version);
    String pathIdAndClassNameAndLimit = ObjectsPathBuilder.builder()
      .id(id).className(className).limit(limit)
      .build().buildPath(version);
    String pathIdAndClassNameAndAdditional = ObjectsPathBuilder.builder()
      .id(id).className(className).additional(additional)
      .build().buildPath(version);
    String pathIdAndClassNameAndLimitAndAdditional = ObjectsPathBuilder.builder()
      .id(id).className(className).limit(limit).additional(additional)
      .build().buildPath(version);
    // then
    assertEquals("/objects/some-uuid", pathIdAndClassName);
    assertEquals("/objects/some-uuid?include=vector,fp", pathIdAndClassNameAndAdditional);
    assertEquals("/objects/some-uuid?limit=20", pathIdAndClassNameAndLimit);
    assertEquals("/objects/some-uuid?include=vector,fp&limit=20", pathIdAndClassNameAndLimitAndAdditional);
  }
}