package technology.semi.weaviate.client.v1.graphql.query.argument;

import junit.framework.TestCase;
import org.junit.Test;

public class SortArgumentsTest extends TestCase {

  @Test
  public void testBuild() {
    // given
    SortArgument sort = SortArgument.builder().path(new String[]{"property"}).order(SortOrder.asc).build();
    // when
    String res = SortArguments.builder().sort(new SortArgument[]{sort}).build().build();
    // then
    assertNotNull(res);
    assertEquals("sort:[{path:[\"property\"] order:asc}]", res);
  }

  @Test
  public void testBuildWithMoreThenOneSortArgument() {
    // given
    SortArgument sort1 = SortArgument.builder().path(new String[]{"property"}).order(SortOrder.asc).build();
    SortArgument sort2 = SortArgument.builder().path(new String[]{"other"}).order(SortOrder.desc).build();
    SortArgument sort3 = SortArgument.builder().path(new String[]{"no-sort-order"}).build();
    // when
    String res = SortArguments.builder().sort(new SortArgument[]{sort1, sort2, sort3}).build().build();
    // then
    assertNotNull(res);
    assertEquals("sort:[{path:[\"property\"] order:asc}, {path:[\"other\"] order:desc}, {path:[\"no-sort-order\"]}]", res);
  }
}