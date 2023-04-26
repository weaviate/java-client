package io.weaviate.client.v1.graphql.query.argument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class GroupByArgumentTest {

  @Test
  public void testBuild() {
    // given
    // when
    String arg = GroupByArgument.builder()
      .path(new String[]{ "path" })
      .groups(1)
      .objectsPerGroup(2).build().build();
    // then
    assertNotNull(arg);
    assertEquals("groupBy:{path:[\"path\"] groups:1 objectsPerGroup:2}", arg);
  }

  @Test
  public void testBuildOnlyPath() {
    // given
    // when
    String arg = GroupByArgument.builder()
      .path(new String[]{ "path" })
      .build().build();
    // then
    assertNotNull(arg);
    assertEquals("groupBy:{path:[\"path\"]}", arg);
  }
}
