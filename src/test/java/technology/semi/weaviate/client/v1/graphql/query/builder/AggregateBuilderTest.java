package technology.semi.weaviate.client.v1.graphql.query.builder;

import junit.framework.TestCase;
import org.junit.Test;
import technology.semi.weaviate.client.v1.graphql.query.argument.AskArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.WhereArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.WhereOperator;
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

  @Test
  public void testBuildAggregateWithGroupByAndLimit() {
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
    String query = AggregateBuilder.builder().className("Pizza").fields(fields).
      groupByClausePropertyName("name").limit(10)
      .build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza(groupBy: \"name\", limit: 10){groupedBy{value} name{count}}}}", query);
  }

  @Test
  public void testBuildAggregateWithWhere() {
    // given
    WhereArgument where = WhereArgument.builder()
      .path(new String[]{ "name" })
      .operator(WhereOperator.Equal)
      .valueString("Hawaii")
      .build();
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{ Field.builder().name("count").build() })
      .build();
    Fields fields = Fields.builder().fields(new Field[]{ meta }).build();
    // when
    String query = AggregateBuilder.builder().className("Pizza").fields(fields).withWhereArgument(where).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza(where:{path:[\"name\"] valueString:\"Hawaii\" operator:Equal}){meta{count}}}}", query);
  }

  @Test
  public void testBuildAggregateWithWhereAndGroupedBy() {
    // given
    WhereArgument where = WhereArgument.builder()
      .path(new String[]{ "name" })
      .operator(WhereOperator.Equal)
      .valueString("Hawaii")
      .build();
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{ Field.builder().name("count").build() })
      .build();
    Fields fields = Fields.builder().fields(new Field[]{ meta }).build();
    // when
    String query = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .groupByClausePropertyName("name")
      .withWhereArgument(where)
      .build()
      .buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza(groupBy: \"name\", where:{path:[\"name\"] valueString:\"Hawaii\" operator:Equal}){meta{count}}}}", query);
  }

  @Test
  public void testBuildAggregateWithNearVector() {
    // given
    NearVectorArgument nearVector = NearVectorArgument.builder().vector(new Float[]{ 0f, 1f, 0.8f }).certainty(0.8f).build();
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{ Field.builder().name("count").build() })
      .build();
    Fields fields = Fields.builder().fields(new Field[]{ meta }).build();
    // when
    String query = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withNearVectorFilter(nearVector).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza(nearVector: {vector: [0.0, 1.0, 0.8] certainty: 0.8}){meta{count}}}}", query);
  }

  @Test
  public void testBuildAggregateWithNearObject() {
    // given
    NearObjectArgument nearObject = NearObjectArgument.builder().id("some-uuid").certainty(0.8f).build();
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{ Field.builder().name("count").build() })
      .build();
    Fields fields = Fields.builder().fields(new Field[]{ meta }).build();
    // when
    String query = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withNearObjectFilter(nearObject).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza(nearObject: {id: \"some-uuid\" certainty: 0.8}){meta{count}}}}", query);
  }

  @Test
  public void testBuildAggregateWithAsk() {
    // given
    AskArgument ask = AskArgument.builder().question("question?").rerank(true).certainty(0.8f).build();
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{ Field.builder().name("count").build() })
      .build();
    Fields fields = Fields.builder().fields(new Field[]{ meta }).build();
    // when
    String query = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withAskArgument(ask).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza(ask: {question: \"question?\" certainty: 0.8 rerank: true}){meta{count}}}}", query);
  }

  @Test
  public void testBuildAggregateWithNearImage() {
    // given
    NearImageArgument nearImage = NearImageArgument.builder().image("iVBORw0KGgoAAAANS").certainty(0.8f).build();
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{ Field.builder().name("count").build() })
      .build();
    Fields fields = Fields.builder().fields(new Field[]{ meta }).build();
    // when
    String query = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withNearImageFilter(nearImage).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza(nearImage: {image: \"iVBORw0KGgoAAAANS\" certainty: 0.8}){meta{count}}}}", query);
  }

  @Test
  public void testBuildAggregateWithObjectLimit() {
    // given
    NearImageArgument nearImage = NearImageArgument.builder().image("iVBORw0KGgoAAAANS").certainty(0.8f).build();
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{ Field.builder().name("count").build() })
      .build();
    Fields fields = Fields.builder().fields(new Field[]{ meta }).build();
    // when
    String query = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withNearImageFilter(nearImage)
      .objectLimit(100).build()
      .buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza(nearImage: {image: \"iVBORw0KGgoAAAANS\" certainty: 0.8}, objectLimit: 100){meta{count}}}}", query);
  }
}