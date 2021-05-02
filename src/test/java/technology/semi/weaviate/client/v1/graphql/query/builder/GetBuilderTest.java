package technology.semi.weaviate.client.v1.graphql.query.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import junit.framework.TestCase;
import org.junit.Test;
import technology.semi.weaviate.client.v1.graphql.query.argument.AskArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;

public class GetBuilderTest extends TestCase {

  @Test
  public void testBuildSimpleGet() {
    // given
    // when
    String query = GetBuilder.builder().className("Pizza").fields("name").build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza{name}}}", query);
  }

  @Test
  public void testBuildGetMultipleFields() {
    // given
    // when
    String query = GetBuilder.builder().className("Pizza").fields("name description").build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza{name description}}}", query);
  }

  @Test
  public void testBuildGetWhereFilter() {
    // given
    String where1 = "{path: [\"name\"] operator: Equal valueString: \"Hawaii\" }";
    String where2 = "{operator: Or operands: [{path: [\"name\"] operator: Equal valueString: \"Hawaii\"}, {path: [\"name\"] operator: Equal valueString: \"Doener\"}]}";
    // when
    String query1 = GetBuilder.builder().className("Pizza").fields("name").withWhereFilter(where1).build().buildQuery();
    String query2 = GetBuilder.builder().className("Pizza").fields("name").withWhereFilter(where2).build().buildQuery();
    // then
    assertNotNull(query1);
    assertEquals("{Get{Pizza(where: {path: [\"name\"] operator: Equal valueString: \"Hawaii\" }){name}}}", query1);
    assertNotNull(query2);
    assertEquals("{Get{Pizza" +
            "(where: {operator: Or operands: [{path: [\"name\"] operator: Equal valueString: \"Hawaii\"}, {path: [\"name\"] operator: Equal valueString: \"Doener\"}]})" +
            "{name}}}", query2);
  }

  @Test
  public void testBuildGetWithLimit() {
    // given
    // when
    String query = GetBuilder.builder().className("Pizza").fields("name").limit(2).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(limit: 2){name}}}", query);
  }

  @Test
  public void testBuildGetWithNearText() {
    // given
    NearTextArgument nearText = NearTextArgument.builder().concepts(new String[]{"good"}).build();
    // when
    String query = GetBuilder.builder().className("Pizza").fields("name").withNearTextFilter(nearText).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(nearText: {concepts: [\"good\"]}){name}}}", query);
  }

  @Test
  public void testBuildGetWithNearVector() {
    // given
    Float[] nearVector = new Float[]{0f, 1f, 0.8f};
    // when
    String query = GetBuilder.builder().className("Pizza").fields("name").withNearVectorFilter(nearVector).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(nearVector: {vector: [0.0,1.0,0.8]}){name}}}", query);
  }

  @Test
  public void testBuildGetWithGroupFilter() {
    // given
    String group = "{type: closest force: 0.4}";
    // when
    String query = GetBuilder.builder().className("Pizza").fields("name").withGroupFilter(group).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(group: {type: closest force: 0.4}){name}}}", query);
  }

  @Test
  public void testBuildGetWithMultipleFilter() {
    // given
    String fields = "name";
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(new String[]{"good"})
            .build();
    String where = "{path: [\"name\"] operator: Equal valueString: \"Hawaii\"}";
    Integer limit = 2;
    // when
    String query = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearTextFilter(nearText).withWhereFilter(where).limit(limit)
            .build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(where: {path: [\"name\"] operator: Equal valueString: \"Hawaii\"}, nearText: {concepts: [\"good\"]}, limit: 2){name}}}", query);
  }

  @Test
  public void testBuildGetWithMultipleFilters() {
    // given
    String fields = "name";
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(new String[]{"good"})
            .build();
    String where = "{path: [\"name\"] operator: Equal valueString: \"Hawaii\"}";
    Float[] nearVector = new Float[]{0f, 1f, 0.8f};
    Integer limit = 2;
    // when
    String query = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearTextFilter(nearText).withWhereFilter(where).withNearVectorFilter(nearVector).limit(limit)
            .build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(where: {path: [\"name\"] operator: Equal valueString: \"Hawaii\"}, " +
            "nearText: {concepts: [\"good\"]}, nearVector: {vector: [0.0,1.0,0.8]}, limit: 2){name}}}", query);
  }

  @Test
  public void testBuildGetWithNearTextWithConcepts() {
    // given
    String fields = "name";
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(new String[]{"good"})
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
  public void testBuildGetWithAsk() {
    // given
    String fields = "name";
    AskArgument ask1 = AskArgument.builder()
            .question("Who are you?")
            .build();
    AskArgument ask2 = AskArgument.builder()
            .question("Who are you?")
            .properties(new String[]{"prop1", "prop2"})
            .build();
    AskArgument ask3 = AskArgument.builder()
            .question("Who are you?")
            .properties(new String[]{"prop1", "prop2"})
            .certainty(0.1f)
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
    // then
    assertNotNull(query1);
    assertEquals("{Get{Pizza(ask: {question: \"Who are you?\"}){name}}}", query1);
    assertNotNull(query2);
    assertEquals("{Get{Pizza(ask: {question: \"Who are you?\" properties: [\"prop1\", \"prop2\"]}){name}}}", query2);
    assertNotNull(query3);
    assertEquals("{Get{Pizza(ask: {question: \"Who are you?\" properties: [\"prop1\", \"prop2\"] certainty: 0.1}){name}}}", query3);
  }

  @Test
  public void testBuildGetWithNearImage() throws FileNotFoundException {
    // given
    File imageFile = new File("src/test/resources/image/pixel.png");
    String base64File = new BufferedReader(new InputStreamReader(new FileInputStream("src/test/resources/image/base64.txt")))
            .lines().collect(Collectors.joining("\n"));
    String image = "data:image/png;base64,iVBORw0KGgoAAAANS";
    String expectedImage = "iVBORw0KGgoAAAANS";
    NearImageArgument nearImage1 = NearImageArgument.builder().imageFile(imageFile).build();
    NearImageArgument nearImage2 = NearImageArgument.builder().imageFile(imageFile).certainty(0.4f).build();
    NearImageArgument nearImage3 = NearImageArgument.builder().image(image).certainty(0.1f).build();
    String fields = "name";
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
}