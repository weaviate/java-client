package io.weaviate.client.v1.graphql.query.argument;

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

  @Test
  public void testBuildWithoutAll() {
    // given
    // when
    String res1 = SortArguments.builder().sort(null).build().build();
    String res2 = SortArguments.builder().sort(new SortArgument[]{SortArgument.builder().build()}).build().build();
    // then
    // builder will return a faulty sort arg in order for Weaviate to error
    // so that user will know that something was wrong
    assertNotNull(res1);
    assertEquals("sort:[]", res1);
    assertNotNull(res2);
    assertEquals("sort:[{}]", res2);
  }
}
