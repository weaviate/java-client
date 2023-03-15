package io.weaviate.client.v1.graphql.query.argument;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SortArgumentTest {

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

  @Test
  public void testBuildWithoutAll() {
    // given
    // when
    String arg = SortArgument.builder().build().build();
    // then
    assertNotNull(arg);
    // builder will return a faulty nearObject arg in order for Weaviate to error
    // so that user will know that something was wrong
    assertEquals("{}", arg);
  }
}
