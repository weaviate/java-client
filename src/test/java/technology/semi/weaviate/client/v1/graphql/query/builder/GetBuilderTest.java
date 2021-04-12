package technology.semi.weaviate.client.v1.graphql.query.builder;

import junit.framework.TestCase;
import org.junit.Test;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;

public class GetBuilderTest extends TestCase {

  @Test
  public void testBuildSimpleGet() {
    // given
    // when
    String query = GetBuilder.builder().className("Pizza").fields("name").build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get {Pizza  {name}}}", query);
  }

  @Test
  public void testBuildGetMultipleFields() {
    // given
    // when
    String query = GetBuilder.builder().className("Pizza").fields("name description").build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get {Pizza  {name description}}}", query);
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
    assertEquals("{Get {Pizza (where: {path: [\"name\"] operator: Equal valueString: \"Hawaii\" }) {name}}}", query1);
    assertNotNull(query2);
    assertEquals("{Get {Pizza " +
            "(where: {operator: Or operands: [{path: [\"name\"] operator: Equal valueString: \"Hawaii\"}, {path: [\"name\"] operator: Equal valueString: \"Doener\"}]}) " +
            "{name}}}", query2);
  }

  @Test
  public void testBuildGetWithLimit() {
    // given
    // when
    String query = GetBuilder.builder().className("Pizza").fields("name").limit(2).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get {Pizza (limit: 2) {name}}}", query);
  }

  @Test
  public void testBuildGetWithNearText() {
    // given
    NearTextArgument nearText = NearTextArgument.builder().concepts(new String[]{"good"}).build();
    // when
    String query = GetBuilder.builder().className("Pizza").fields("name").withNearTextFilter(nearText).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get {Pizza (nearText: {concepts: [\"good\"] }) {name}}}", query);
  }

  @Test
  public void testBuildGetWithNearVector() {
    // given
    Float[] nearVector = new Float[]{0f, 1f, 0.8f};
    // when
    String query = GetBuilder.builder().className("Pizza").fields("name").withNearVectorFilter(nearVector).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get {Pizza (nearVector: {vector: [0.0,1.0,0.8]}) {name}}}", query);
  }

  @Test
  public void testBuildGetWithGroupFilter() {
    // given
    String group = "{type: closest force: 0.4}";
    // when
    String query = GetBuilder.builder().className("Pizza").fields("name").withGroupFilter(group).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get {Pizza (group: {type: closest force: 0.4}) {name}}}", query);
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
    assertEquals("{Get {Pizza (where: {path: [\"name\"] operator: Equal valueString: \"Hawaii\"}, nearText: {concepts: [\"good\"] }, limit: 2) {name}}}", query);
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
    assertEquals("{Get {Pizza (where: {path: [\"name\"] operator: Equal valueString: \"Hawaii\"}, " +
            "nearText: {concepts: [\"good\"] }, nearVector: {vector: [0.0,1.0,0.8]}, limit: 2) {name}}}", query);
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
    assertEquals("{Get {Pizza (nearText: {concepts: [\"good\"] }) {name}}}", query);
  }
}