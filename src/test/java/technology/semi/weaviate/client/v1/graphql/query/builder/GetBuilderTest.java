package technology.semi.weaviate.client.v1.graphql.query.builder;

import junit.framework.TestCase;
import org.junit.Test;
import technology.semi.weaviate.client.v1.filters.Operator;
import technology.semi.weaviate.client.v1.filters.WhereFilter;
import technology.semi.weaviate.client.v1.graphql.query.argument.AskArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.GroupArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.GroupType;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.SortArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.SortArguments;
import technology.semi.weaviate.client.v1.graphql.query.argument.SortOrder;
import technology.semi.weaviate.client.v1.graphql.query.fields.Field;
import technology.semi.weaviate.client.v1.graphql.query.fields.Fields;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class GetBuilderTest extends TestCase {

  @Test
  public void testBuildSimpleGet() {
    // given
    Field name = Field.builder().name("name").build();
    // when
    String query = GetBuilder.builder().className("Pizza").fields(Fields.builder().fields(new Field[]{ name }).build()).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza{name}}}", query);
  }

  @Test
  public void testBuildGetMultipleFields() {
    // given
    Field name = Field.builder().name("name").build();
    Field description = Field.builder().name("description").build();
    Fields fields = Fields.builder().fields(new Field[]{ name, description }).build();
    // when
    String query = GetBuilder.builder().className("Pizza").fields(fields).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza{name description}}}", query);
  }

  public void testBuildGetWhereFilter() {
    // given
    Field name = Field.builder().name("name").build();
    Fields fields = Fields.builder().fields(new Field[]{ name }).build();
    WhereFilter where1 = WhereFilter.builder()
            .path(new String[]{ "name" }).operator(Operator.Equal).valueString("Hawaii")
            .build();
    WhereFilter where2 = WhereFilter.builder()
            .operands(new WhereFilter[]{
                    WhereFilter.builder()
                            .path(new String[]{ "name" }).operator(Operator.Equal).valueString("Hawaii")
                            .build(),
                    WhereFilter.builder()
                            .path(new String[]{ "name" }).operator(Operator.Equal).valueString("Doener")
                            .build(),
            })
            .operator(Operator.Or)
            .build();
    // when
    String query1 = GetBuilder.builder().className("Pizza").fields(fields).withWhereFilter(where1).build().buildQuery();
    String query2 = GetBuilder.builder().className("Pizza").fields(fields).withWhereFilter(where2).build().buildQuery();
    // then
    assertNotNull(query1);
    assertEquals("{Get{Pizza(where:{path:[\"name\"] valueString:\"Hawaii\" operator:Equal}){name}}}", query1);
    assertNotNull(query2);
    assertEquals("{Get{Pizza" +
            "(where:{operator:Or operands:[{path:[\"name\"] valueString:\"Hawaii\" operator:Equal},{path:[\"name\"] valueString:\"Doener\" operator:Equal}]})" +
            "{name}}}", query2);
  }

  @Test
  public void testBuildGetWithLimit() {
    // given
    Field name = Field.builder().name("name").build();
    Fields fields = Fields.builder().fields(new Field[]{ name }).build();
    // when
    String query = GetBuilder.builder()
            .className("Pizza")
            .fields(fields)
            .limit(2)
            .build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(limit: 2){name}}}", query);
  }

  @Test
  public void testBuildGetWithLimitAndOffset() {
    // given
    Field name = Field.builder().name("name").build();
    Fields fields = Fields.builder().fields(new Field[]{ name }).build();
    // when
    String query = GetBuilder.builder()
            .className("Pizza")
            .fields(fields)
            .offset(0)
            .limit(2)
            .build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(limit: 2, offset: 0){name}}}", query);
  }

  @Test
  public void testBuildGetWithLimitAndAfter() {
    // given
    Field name = Field.builder().name("name").build();
    Fields fields = Fields.builder().fields(new Field[]{ name }).build();
    // when
    String query = GetBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .after("00000000-0000-0000-0000-000000000000")
      .limit(2)
      .build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(limit: 2, after: \"00000000-0000-0000-0000-000000000000\"){name}}}", query);
  }

  @Test
  public void testBuildGetWithNearText() {
    // given
    Field name = Field.builder().name("name").build();
    NearTextArgument nearText = NearTextArgument.builder().concepts(new String[]{ "good" }).build();
    // when
    String query = GetBuilder.builder()
            .className("Pizza")
            .fields(Fields.builder().fields(new Field[]{ name }).build())
            .withNearTextFilter(nearText).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(nearText: {concepts: [\"good\"]}){name}}}", query);
  }

  @Test
  public void testBuildGetWithNearVector() {
    Field name = Field.builder().name("name").build();

    // given (certainty)
    NearVectorArgument nearVectorWithCert = NearVectorArgument.builder()
            .vector(new Float[]{ 0f, 1f, 0.8f }).certainty(0.8f).build();
    // when (certainty)
    String queryWithCert = GetBuilder.builder().className("Pizza")
            .fields(Fields.builder().fields(new Field[]{ name }).build())
            .withNearVectorFilter(nearVectorWithCert).build().buildQuery();
    // then (certainty)
    assertNotNull(queryWithCert);
    assertEquals("{Get{Pizza(nearVector: {vector: [0.0, 1.0, 0.8] certainty: 0.8}){name}}}", queryWithCert);

    // given (distance)
    NearVectorArgument nearVectorWithDist = NearVectorArgument.builder()
            .vector(new Float[]{ 0f, 1f, 0.8f }).distance(0.8f).build();
    // when (distance)
    String queryWithDist = GetBuilder.builder().className("Pizza")
            .fields(Fields.builder().fields(new Field[]{ name }).build())
            .withNearVectorFilter(nearVectorWithDist).build().buildQuery();
    // then (distance)
    assertNotNull(queryWithDist);
    assertEquals("{Get{Pizza(nearVector: {vector: [0.0, 1.0, 0.8] distance: 0.8}){name}}}", queryWithDist);
  }

  @Test
  public void testBuildGetWithGroupFilter() {
    // given
    Field name = Field.builder().name("name").build();
    GroupArgument group = GroupArgument.builder().type(GroupType.closest).force(0.4f).build();
    // when
    String query = GetBuilder.builder().className("Pizza")
            .fields(Fields.builder().fields(new Field[]{ name }).build())
            .withGroupArgument(group).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(group:{type: closest force: 0.4}){name}}}", query);
  }

  @Test
  public void testBuildGetWithMultipleFilter() {
    // given
    Fields fields = Fields.builder()
            .fields(new Field[]{ Field.builder().name("name").build() })
            .build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(new String[]{ "good" })
            .build();
    WhereFilter where = WhereFilter.builder()
            .path(new String[]{ "name" })
            .operator(Operator.Equal)
            .valueString("Hawaii")
            .build();
    Integer limit = 2;
    // when
    String query = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearTextFilter(nearText).withWhereFilter(where).limit(limit)
            .build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(where:{path:[\"name\"] valueString:\"Hawaii\" operator:Equal}, nearText: {concepts: [\"good\"]}, limit: 2){name}}}", query);
  }

  @Test
  public void testBuildGetWithNearTextWithConcepts() {
    // given
    Fields fields = Fields.builder()
            .fields(new Field[]{ Field.builder().name("name").build() })
            .build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(new String[]{ "good" })
            .build();
    // when
    String query = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearTextFilter(nearText)
            .build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(nearText: {concepts: [\"good\"]}){name}}}", query);
  }

  @Test
  public void testBuildGetWithAskAndCertainty() {
    // given
    Fields fields = Fields.builder()
            .fields(new Field[]{ Field.builder().name("name").build() })
            .build();
    AskArgument ask1 = AskArgument.builder()
            .question("Who are you?")
            .build();
    AskArgument ask2 = AskArgument.builder()
            .question("Who are you?")
            .properties(new String[]{ "prop1", "prop2" })
            .build();
    AskArgument ask3 = AskArgument.builder()
            .question("Who are you?")
            .properties(new String[]{ "prop1", "prop2" })
            .certainty(0.1f)
            .build();
    AskArgument ask4 = AskArgument.builder()
            .question("Who are you?")
            .properties(new String[]{ "prop1", "prop2" })
            .certainty(0.1f)
            .rerank(true)
            .build();
    // when
    String query1 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask1)
            .build().buildQuery();
    String query2 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask2)
            .build().buildQuery();
    String query3 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask3)
            .build().buildQuery();
    String query4 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask4)
            .build().buildQuery();
    // then
    assertNotNull(query1);
    assertEquals("{Get{Pizza(ask: {question: \"Who are you?\"}){name}}}", query1);
    assertNotNull(query2);
    assertEquals("{Get{Pizza(ask: {question: \"Who are you?\" properties: [\"prop1\", \"prop2\"]}){name}}}", query2);
    assertNotNull(query3);
    assertEquals("{Get{Pizza(ask: {question: \"Who are you?\" properties: [\"prop1\", \"prop2\"] certainty: 0.1}){name}}}", query3);
    assertNotNull(query4);
    assertEquals("{Get{Pizza(ask: {question: \"Who are you?\" properties: [\"prop1\", \"prop2\"] certainty: 0.1 rerank: true}){name}}}", query4);
  }

  @Test
  public void testBuildGetWithAskAndDistance() {
    // given
    Fields fields = Fields.builder()
            .fields(new Field[]{ Field.builder().name("name").build() })
            .build();
    AskArgument ask1 = AskArgument.builder()
            .question("Who are you?")
            .build();
    AskArgument ask2 = AskArgument.builder()
            .question("Who are you?")
            .properties(new String[]{ "prop1", "prop2" })
            .build();
    AskArgument ask3 = AskArgument.builder()
            .question("Who are you?")
            .properties(new String[]{ "prop1", "prop2" })
            .distance(0.1f)
            .build();
    AskArgument ask4 = AskArgument.builder()
            .question("Who are you?")
            .properties(new String[]{ "prop1", "prop2" })
            .distance(0.1f)
            .rerank(true)
            .build();
    // when
    String query1 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask1)
            .build().buildQuery();
    String query2 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask2)
            .build().buildQuery();
    String query3 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask3)
            .build().buildQuery();
    String query4 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask4)
            .build().buildQuery();
    // then
    assertNotNull(query1);
    assertEquals("{Get{Pizza(ask: {question: \"Who are you?\"}){name}}}", query1);
    assertNotNull(query2);
    assertEquals("{Get{Pizza(ask: {question: \"Who are you?\" properties: [\"prop1\", \"prop2\"]}){name}}}", query2);
    assertNotNull(query3);
    assertEquals("{Get{Pizza(ask: {question: \"Who are you?\" properties: [\"prop1\", \"prop2\"] distance: 0.1}){name}}}", query3);
    assertNotNull(query4);
    assertEquals("{Get{Pizza(ask: {question: \"Who are you?\" properties: [\"prop1\", \"prop2\"] distance: 0.1 rerank: true}){name}}}", query4);
  }

  @Test
  public void testBuildGetWithNearImageAndCertainty() throws FileNotFoundException {
    // given
    File imageFile = new File("src/test/resources/image/pixel.png");
    String base64File = new BufferedReader(new InputStreamReader(new FileInputStream("src/test/resources/image/base64.txt")))
            .lines().collect(Collectors.joining("\n"));
    String image = "data:image/png;base64,iVBORw0KGgoAAAANS";
    String expectedImage = "iVBORw0KGgoAAAANS";
    NearImageArgument nearImage1 = NearImageArgument.builder().imageFile(imageFile).build();
    NearImageArgument nearImage2 = NearImageArgument.builder().imageFile(imageFile).certainty(0.4f).build();
    NearImageArgument nearImage3 = NearImageArgument.builder().image(image).certainty(0.1f).build();
    Fields fields = Fields.builder()
            .fields(new Field[]{ Field.builder().name("name").build() })
            .build();
    // when
    String query1 = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearImageFilter(nearImage1)
            .build().buildQuery();
    String query2 = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearImageFilter(nearImage2)
            .build().buildQuery();
    String query3 = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearImageFilter(nearImage3).limit(1)
            .build().buildQuery();
    assertNotNull(query1);
    assertEquals(String.format("{Get{Pizza(nearImage: {image: \"%s\"}){name}}}", base64File), query1);
    assertNotNull(query2);
    assertEquals(String.format("{Get{Pizza(nearImage: {image: \"%s\" certainty: 0.4}){name}}}", base64File), query2);
    assertNotNull(query3);
    assertEquals(String.format("{Get{Pizza(nearImage: {image: \"%s\" certainty: 0.1}, limit: 1){name}}}", expectedImage), query3);
  }

  @Test
  public void testBuildGetWithNearImageAndDistance() throws FileNotFoundException {
    // given
    File imageFile = new File("src/test/resources/image/pixel.png");
    String base64File = new BufferedReader(new InputStreamReader(new FileInputStream("src/test/resources/image/base64.txt")))
            .lines().collect(Collectors.joining("\n"));
    String image = "data:image/png;base64,iVBORw0KGgoAAAANS";
    String expectedImage = "iVBORw0KGgoAAAANS";
    NearImageArgument nearImage1 = NearImageArgument.builder().imageFile(imageFile).build();
    NearImageArgument nearImage2 = NearImageArgument.builder().imageFile(imageFile).distance(0.4f).build();
    NearImageArgument nearImage3 = NearImageArgument.builder().image(image).distance(0.1f).build();
    Fields fields = Fields.builder()
            .fields(new Field[]{ Field.builder().name("name").build() })
            .build();
    // when
    String query1 = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearImageFilter(nearImage1)
            .build().buildQuery();
    String query2 = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearImageFilter(nearImage2)
            .build().buildQuery();
    String query3 = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearImageFilter(nearImage3).limit(1)
            .build().buildQuery();
    assertNotNull(query1);
    assertEquals(String.format("{Get{Pizza(nearImage: {image: \"%s\"}){name}}}", base64File), query1);
    assertNotNull(query2);
    assertEquals(String.format("{Get{Pizza(nearImage: {image: \"%s\" distance: 0.4}){name}}}", base64File), query2);
    assertNotNull(query3);
    assertEquals(String.format("{Get{Pizza(nearImage: {image: \"%s\" distance: 0.1}, limit: 1){name}}}", expectedImage), query3);
  }

  @Test
  public void testBuildGetWithSort() {
    // given
    Fields fields = Fields.builder()
            .fields(new Field[]{ Field.builder().name("name").build() })
            .build();
    SortArgument sort1 = SortArgument.builder().path(new String[]{ "property1" }).build();
    SortArgument sort2 = SortArgument.builder().path(new String[]{ "property2" }).order(SortOrder.desc).build();
    SortArgument sort3 = SortArgument.builder().path(new String[]{ "property3" }).order(SortOrder.asc).build();
    // when
    String query1 = GetBuilder.builder().className("Pizza").fields(fields)
            .withSortArguments(SortArguments.builder().sort(new SortArgument[]{sort1}).build())
            .build().buildQuery();
    String query2 = GetBuilder.builder().className("Pizza").fields(fields)
            .withSortArguments(SortArguments.builder().sort(new SortArgument[]{sort1, sort2}).build())
            .build().buildQuery();
    String query3 = GetBuilder.builder().className("Pizza").fields(fields)
            .withSortArguments(SortArguments.builder().sort(new SortArgument[]{sort1, sort2, sort3}).build())
            .build().buildQuery();
    // then
    assertNotNull(query1);
    assertEquals("{Get{Pizza(sort:[{path:[\"property1\"]}]){name}}}", query1);
    assertEquals("{Get{Pizza(sort:[{path:[\"property1\"]}, {path:[\"property2\"] order:desc}]){name}}}", query2);
    assertEquals("{Get{Pizza(sort:[{path:[\"property1\"]}, {path:[\"property2\"] order:desc}, {path:[\"property3\"] order:asc}]){name}}}", query3);
  }
}
