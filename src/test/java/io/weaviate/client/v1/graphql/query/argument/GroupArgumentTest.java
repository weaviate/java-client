package io.weaviate.client.v1.graphql.query.argument;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GroupArgumentTest {

  @Test
  public void testBuildWithAllParameters() {
    // given
    String expected = "group:{type:merge force:0.05}";
    GroupArgument groupArgument = GroupArgument.builder().type(GroupType.merge).force(0.05f).build();
    // when
    String result = groupArgument.build();
    // then
    assertEquals(expected, result);
  }

  @Test
  public void testBuildWithType() {
    // given
    String expected = "group:{type:closest}";
    GroupArgument groupArgument = GroupArgument.builder().type(GroupType.closest).build();
    // when
    String result = groupArgument.build();
    // then
    assertEquals(expected, result);
  }

  @Test
  public void testBuildWithForce() {
    // given
    String expected = "group:{force:0.9}";
    GroupArgument groupArgument = GroupArgument.builder().force(0.90f).build();
    // when
    String result = groupArgument.build();
    // then
    assertEquals(expected, result);
  }

  @Test
  public void testBuildWithoutAll() {
    // given
    GroupArgument groupArgument = GroupArgument.builder().build();
    // when
    String result = groupArgument.build();
    // then
    // builder will return a faulty group arg in order for Weaviate to error
    // so that user will know that something was wrong
    assertEquals("group:{}", result);
  }
}
