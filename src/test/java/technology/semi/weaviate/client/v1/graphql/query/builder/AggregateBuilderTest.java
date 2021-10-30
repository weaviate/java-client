package technology.semi.weaviate.client.v1.graphql.query.builder;

import junit.framework.TestCase;
import org.junit.Test;
import technology.semi.weaviate.client.v1.graphql.query.fields.Field;
import technology.semi.weaviate.client.v1.graphql.query.fields.Fields;

public class AggregateBuilderTest extends TestCase {

  @Test
  public void testBuildSimpleAggregate() {
    // given
    Field meta = Field.builder()
            .name("meta")
            .fields(new Field[]{ Field.builder().name("count").build() })
            .build();
    Fields fields = Fields.builder().fields(new Field[]{ meta }).build();
    // when
    String query = AggregateBuilder.builder().className("Pizza").fields(fields).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza{meta{count}}}}", query);
  }

  @Test
  public void testBuildAggregateWithGroupBy() {
    // given
    Field groupBy = Field.builder()
            .name("groupedBy")
            .fields(new Field[]{ Field.builder().name("value").build() })
            .build();
    Field name = Field.builder()
            .name("name")
            .fields(new Field[]{ Field.builder().name("count").build() })
            .build();
    Fields fields = Fields.builder().fields(new Field[]{ groupBy, name }).build();
    // when
    String query = AggregateBuilder.builder().className("Pizza").fields(fields).groupByClausePropertyName("name").build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza(groupBy: \"name\"){groupedBy{value} name{count}}}}", query);
  }
}