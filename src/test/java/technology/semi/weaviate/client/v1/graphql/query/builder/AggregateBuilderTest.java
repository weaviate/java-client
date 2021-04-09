package technology.semi.weaviate.client.v1.graphql.query.builder;

import junit.framework.TestCase;
import org.junit.Test;

public class AggregateBuilderTest extends TestCase {

  @Test
  public void testBuildSimpleAggregate() {
    // given
    // when
    String query = AggregateBuilder.builder().className("Pizza").fields("meta {count}").build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza{meta {count}}}}", query);
  }

  @Test
  public void testBuildAggregateWithGroupBy() {
    // given
    String fields = "groupedBy {value}name {count}";
    // when
    String query = AggregateBuilder.builder().className("Pizza").fields(fields).groupByClausePropertyName("name").build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza(groupBy: \"name\"){groupedBy {value}name {count}}}}", query);
  }
}