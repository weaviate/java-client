package technology.semi.weaviate.client.v1.graphql.query.argument;

import junit.framework.TestCase;
import org.junit.Test;

public class SortArgumentTest extends TestCase {

  @Test
  public void testBuild() {
    // given
    // when
    String arg = SortArgument.builder().path(new String[]{ "property" }).order(SortOrder.asc).build().build();
    // then
    assertNotNull(arg);
    assertEquals("{path:[\"property\"] order:asc}", arg);
  }

  @Test
  public void testBuildWithoutOrder() {
    // given
    // when
    String arg = SortArgument.builder().path(new String[]{ "property" }).build().build();
    // then
    assertNotNull(arg);
    assertEquals("{path:[\"property\"]}", arg);
  }
}