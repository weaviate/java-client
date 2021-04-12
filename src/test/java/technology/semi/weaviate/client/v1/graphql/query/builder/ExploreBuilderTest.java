package technology.semi.weaviate.client.v1.graphql.query.builder;

import junit.framework.TestCase;
import org.junit.Test;
import technology.semi.weaviate.client.v1.graphql.model.ExploreFields;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import technology.semi.weaviate.client.v1.graphql.query.argument.NearTextMoveParameters;

public class ExploreBuilderTest extends TestCase {

  @Test
  public void testBuildQuery() {
    // given
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.CERTAINTY, ExploreFields.BEACON, ExploreFields.CLASS_NAME};
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{"a1", "b2"}).force(0.1f).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(new String[]{"a", "b"}).certainty(0.8f).moveTo(moveTo).build();
    // when
    String query = ExploreBuilder.builder().withNearText(nearText).fields(fields).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Explore(nearText: {concepts: [\"a\", \"b\"] certainty: 0.8 moveTo: {concepts: [\"a1\", \"b2\"] force: 0.1} }){certainty, beacon, className}}", query);
  }

  @Test
  public void testBuildSimpleExplore() {
    // given
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.CERTAINTY, ExploreFields.BEACON};
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(new String[]{"Cheese", "pineapple"}).build();
    // when
    String query = ExploreBuilder.builder().withNearText(nearText).fields(fields).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Explore(nearText: {concepts: [\"Cheese\", \"pineapple\"] }){certainty, beacon}}", query);
  }

  @Test
  public void testBuildExploreWithLmitAndCertainty() {
    // given
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.BEACON};
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(new String[]{"Cheese"}).certainty(0.71f).build();
    // when
    String query = ExploreBuilder.builder().withNearText(nearText).fields(fields).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Explore(nearText: {concepts: [\"Cheese\"] certainty: 0.71 }){beacon}}", query);
  }

  @Test
  public void testBuildExploreWithMove() {
    // given
    String[] concepts = new String[]{"Cheese"};
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{"pizza", "pineapple"}).force(0.2f).build();
    NearTextMoveParameters moveAwayFrom = NearTextMoveParameters.builder()
            .concepts(new String[]{"fish"}).force(0.1f).build();
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.BEACON};
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).moveTo(moveTo).moveAwayFrom(moveAwayFrom)
            .build();
    // when
    String query = ExploreBuilder.builder().withNearText(nearText).fields(fields).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Explore(nearText: {concepts: [\"Cheese\"] " +
            "moveTo: {concepts: [\"pizza\", \"pineapple\"] force: 0.2} " +
            "moveAwayFrom: {concepts: [\"fish\"] force: 0.1} }){beacon}}", query);
  }

  @Test
  public void testBuildExploreWithAllParams() {
    // given
    String[] concepts = new String[]{"New Yorker"};
    Float certainty = 0.95f;
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{"publisher", "articles"}).force(0.5f)
            .build();
    NearTextMoveParameters moveAwayFrom = NearTextMoveParameters.builder()
            .concepts(new String[]{"fashion", "shop"}).force(0.2f)
            .build();
    ExploreFields[] fields = new ExploreFields[]{ExploreFields.CERTAINTY, ExploreFields.BEACON, ExploreFields.CLASS_NAME};
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).moveTo(moveTo).moveAwayFrom(moveAwayFrom)
            .build();
    // when
    String query = ExploreBuilder.builder().withNearText(nearText).fields(fields).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Explore(nearText: {concepts: [\"New Yorker\"] moveTo: {concepts: [\"publisher\", \"articles\"] force: 0.5} moveAwayFrom: {concepts: " +
            "[\"fashion\", \"shop\"] force: 0.2} }){certainty, beacon, className}}", query);
  }
}