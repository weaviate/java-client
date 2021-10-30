package technology.semi.weaviate.client.v1.graphql.query.argument;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class GroupArgumentTest extends TestCase {

  @Test
  public void testBuildWithAllParameters() {
    // given
    String expected = "group:{type: merge force: 0.05}";
    GroupArgument groupArgument = GroupArgument.builder().type(GroupType.merge).force(0.05f).build();
    // when
    String result = groupArgument.build();
    // then
    Assert.assertEquals(expected, result);
  }

  @Test
  public void testBuildWithType() {
    // given
    String expected = "group:{type: closest}";
    GroupArgument groupArgument = GroupArgument.builder().type(GroupType.closest).build();
    // when
    String result = groupArgument.build();
    // then
    Assert.assertEquals(expected, result);
  }

  @Test
  public void testBuildWithForce() {
    // given
    String expected = "group:{force: 0.9}";
    GroupArgument groupArgument = GroupArgument.builder().force(0.90f).build();
    // when
    String result = groupArgument.build();
    // then
    Assert.assertEquals(expected, result);
  }
}