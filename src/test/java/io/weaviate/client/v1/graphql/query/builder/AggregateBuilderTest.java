package io.weaviate.client.v1.graphql.query.builder;

import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.query.argument.AskArgument;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AggregateBuilderTest {

  @Test
  public void testBuildSimpleAggregate() {
    // given
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{Field.builder().name("count").build()})
      .build();
    Fields fields = Fields.builder().fields(new Field[]{meta}).build();
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
      .fields(new Field[]{Field.builder().name("value").build()})
      .build();
    Field name = Field.builder()
      .name("name")
      .fields(new Field[]{Field.builder().name("count").build()})
      .build();
    Fields fields = Fields.builder().fields(new Field[]{groupBy, name}).build();
    // when
    String query = AggregateBuilder.builder().className("Pizza").fields(fields).groupByClausePropertyName("name").build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza(groupBy:\"name\"){groupedBy{value} name{count}}}}", query);
  }

  @Test
  public void testBuildAggregateWithGroupByAndLimit() {
    // given
    Field groupBy = Field.builder()
      .name("groupedBy")
      .fields(new Field[]{Field.builder().name("value").build()})
      .build();
    Field name = Field.builder()
      .name("name")
      .fields(new Field[]{Field.builder().name("count").build()})
      .build();
    Fields fields = Fields.builder().fields(new Field[]{groupBy, name}).build();
    // when
    String query = AggregateBuilder.builder().className("Pizza").fields(fields).
      groupByClausePropertyName("name").limit(10)
      .build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza(groupBy:\"name\" limit:10){groupedBy{value} name{count}}}}", query);
  }

  @Test
  public void testBuildAggregateWithWhere() {
    // given
    WhereArgument where = WhereArgument.builder()
      .filter(WhereFilter.builder()
        .path(new String[]{"name"})
        .operator(Operator.Equal)
        .valueText("Hawaii")
        .build())
      .build();
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{Field.builder().name("count").build()})
      .build();
    Fields fields = Fields.builder().fields(new Field[]{meta}).build();
    // when
    String query = AggregateBuilder.builder().className("Pizza").fields(fields).withWhereFilter(where).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza(where:{path:[\"name\"] valueText:\"Hawaii\" operator:Equal}){meta{count}}}}", query);
  }

  @Test
  public void testBuildAggregateWithWhereAndGroupedBy() {
    // given
    WhereArgument where = WhereArgument.builder()
      .filter(WhereFilter.builder()
        .path(new String[]{"name"})
        .operator(Operator.Equal)
        .valueText("Hawaii")
        .build())
      .build();
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{Field.builder().name("count").build()})
      .build();
    Fields fields = Fields.builder().fields(new Field[]{meta}).build();
    // when
    String query = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .groupByClausePropertyName("name")
      .withWhereFilter(where)
      .build()
      .buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Aggregate{Pizza(groupBy:\"name\" where:{path:[\"name\"] valueText:\"Hawaii\" operator:Equal}){meta{count}}}}", query);
  }

  @Test
  public void testBuildAggregateWithNearVector() {
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{Field.builder().name("count").build()})
      .build();
    Fields fields = Fields.builder().fields(new Field[]{meta}).build();

    // given (certainty)
    NearVectorArgument nearVectorWithCert = NearVectorArgument.builder().vector(new Float[]{0f, 1f, 0.8f}).certainty(0.8f).build();

    // when (certainty)
    String queryWithCert = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withNearVectorFilter(nearVectorWithCert).build().buildQuery();
    // then (certainty)
    assertNotNull(queryWithCert);
    assertEquals("{Aggregate{Pizza(nearVector:{vector:[0.0,1.0,0.8] certainty:0.8}){meta{count}}}}", queryWithCert);

    // given (distance)
    NearVectorArgument nearVectorWithDist = NearVectorArgument.builder().vector(new Float[]{0f, 1f, 0.8f}).distance(0.8f).build();

    // when (distance)
    String queryWithDist = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withNearVectorFilter(nearVectorWithDist).build().buildQuery();
    // then (distance)
    assertNotNull(queryWithDist);
    assertEquals("{Aggregate{Pizza(nearVector:{vector:[0.0,1.0,0.8] distance:0.8}){meta{count}}}}", queryWithDist);
  }

  @Test
  public void testBuildAggregateWithNearObject() {
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{Field.builder().name("count").build()})
      .build();
    Fields fields = Fields.builder().fields(new Field[]{meta}).build();

    // given (certainty)
    NearObjectArgument nearObjectWithCert = NearObjectArgument.builder().id("some-uuid").certainty(0.8f).build();
    // when (certainty)
    String queryWithCert = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withNearObjectFilter(nearObjectWithCert).build().buildQuery();
    // then (certainty)
    assertNotNull(queryWithCert);
    assertEquals("{Aggregate{Pizza(nearObject:{id:\"some-uuid\" certainty:0.8}){meta{count}}}}", queryWithCert);

    // given (distance)
    NearObjectArgument nearObjectWithDist = NearObjectArgument.builder().id("some-uuid").distance(0.8f).build();
    // when (distance)
    String queryWithDist = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withNearObjectFilter(nearObjectWithDist).build().buildQuery();
    // then (distance)
    assertNotNull(queryWithDist);
    assertEquals("{Aggregate{Pizza(nearObject:{id:\"some-uuid\" distance:0.8}){meta{count}}}}", queryWithDist);
  }

  @Test
  public void testBuildAggregateWithAsk() {
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{Field.builder().name("count").build()})
      .build();
    Fields fields = Fields.builder().fields(new Field[]{meta}).build();

    // given (certainty)
    AskArgument askWithCert = AskArgument.builder().question("question?").rerank(true).certainty(0.8f).build();
    // when (certainty)
    String queryWithCert = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withAskArgument(askWithCert).build().buildQuery();
    // then (certainty)
    assertNotNull(queryWithCert);
    assertEquals("{Aggregate{Pizza(ask:{question:\"question?\" certainty:0.8 rerank:true}){meta{count}}}}", queryWithCert);

    // given (distance)
    AskArgument askWithDist = AskArgument.builder().question("question?").rerank(true).distance(0.8f).build();
    // when (distance)
    String queryWithDist = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withAskArgument(askWithDist).build().buildQuery();
    // then (distance)
    assertNotNull(queryWithDist);
    assertEquals("{Aggregate{Pizza(ask:{question:\"question?\" distance:0.8 rerank:true}){meta{count}}}}", queryWithDist);
  }

  @Test
  public void testBuildAggregateWithNearImage() {
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{Field.builder().name("count").build()})
      .build();
    Fields fields = Fields.builder().fields(new Field[]{meta}).build();

    // given (certainty)
    NearImageArgument nearImageWithCert = NearImageArgument.builder().image("iVBORw0KGgoAAAANS").certainty(0.8f).build();
    // when (certainty)
    String queryWithCert = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withNearImageFilter(nearImageWithCert).build().buildQuery();
    // then (certainty)
    assertNotNull(queryWithCert);
    assertEquals("{Aggregate{Pizza(nearImage:{image:\"iVBORw0KGgoAAAANS\" certainty:0.8}){meta{count}}}}", queryWithCert);

    // given (certainty)
    NearImageArgument nearImageWithDist = NearImageArgument.builder().image("iVBORw0KGgoAAAANS").distance(0.8f).build();
    // when (certainty)
    String queryWithDist = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withNearImageFilter(nearImageWithDist).build().buildQuery();
    // then (certainty)
    assertNotNull(queryWithDist);
    assertEquals("{Aggregate{Pizza(nearImage:{image:\"iVBORw0KGgoAAAANS\" distance:0.8}){meta{count}}}}", queryWithDist);
  }

  @Test
  public void testBuildAggregateWithObjectLimit() {
    Field meta = Field.builder()
      .name("meta")
      .fields(new Field[]{Field.builder().name("count").build()})
      .build();
    Fields fields = Fields.builder().fields(new Field[]{meta}).build();

    // given (certainty)
    NearImageArgument nearImageWithCert = NearImageArgument.builder().image("iVBORw0KGgoAAAANS").certainty(0.8f).build();
    // when (certainty)
    String queryWithCert = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withNearImageFilter(nearImageWithCert)
      .objectLimit(100).build()
      .buildQuery();
    // then (certainty)
    assertNotNull(queryWithCert);
    assertEquals("{Aggregate{Pizza(nearImage:{image:\"iVBORw0KGgoAAAANS\" certainty:0.8} objectLimit:100){meta{count}}}}", queryWithCert);

    // given (distance)
    NearImageArgument nearImageWithDist = NearImageArgument.builder().image("iVBORw0KGgoAAAANS").distance(0.8f).build();
    // when (distance)
    String queryWithDist = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withNearImageFilter(nearImageWithDist)
      .objectLimit(100).build()
      .buildQuery();
    // then (distance)
    assertNotNull(queryWithDist);
    assertEquals("{Aggregate{Pizza(nearImage:{image:\"iVBORw0KGgoAAAANS\" distance:0.8} objectLimit:100){meta{count}}}}", queryWithDist);
  }

  @Test
  public void shouldSupportDeprecatedWhereFilter() {
    WhereFilter where = WhereFilter.builder()
      .path(new String[]{"name"})
      .operator(Operator.Equal)
      .valueText("Hawaii")
      .build();
    Field meta = Field.builder()
      .name("meta")
      .fields(Field.builder().name("count").build())
      .build();
    Fields fields = Fields.builder().fields(meta).build();

    String query = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .groupByClausePropertyName("name")
      .withWhereFilter(where)
      .build().buildQuery();

    assertThat(query).isEqualTo("{Aggregate{Pizza(groupBy:\"name\" where:{path:[\"name\"] valueText:\"Hawaii\" operator:Equal}){meta{count}}}}");
  }

  @Test
  public void shouldBuildAggregateWithTenantKey() {
    Field meta = Field.builder()
      .name("meta")
      .fields(Field.builder().name("count").build())
      .build();
    Fields fields = Fields.builder().fields(meta).build();

    String query = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .tenantKey("TenantNo1")
      .build().buildQuery();

    assertThat(query).isEqualTo("{Aggregate{Pizza(tenantKey:\"TenantNo1\"){meta{count}}}}");
  }

  @Test
  public void shouldBuildAggregateWithTenantKeyAndWhere() {
    WhereArgument where = WhereArgument.builder()
      .filter(WhereFilter.builder()
        .path(new String[]{"name"})
        .operator(Operator.Equal)
        .valueText("Hawaii")
        .build())
      .build();
    Field meta = Field.builder()
      .name("meta")
      .fields(Field.builder().name("count").build())
      .build();
    Fields fields = Fields.builder().fields(meta).build();

    String query = AggregateBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .tenantKey("TenantNo1")
      .withWhereFilter(where)
      .build().buildQuery();

    assertThat(query).isEqualTo("{Aggregate{Pizza(tenantKey:\"TenantNo1\" where:{path:[\"name\"] valueText:\"Hawaii\" operator:Equal}){meta{count}}}}");
  }
}
