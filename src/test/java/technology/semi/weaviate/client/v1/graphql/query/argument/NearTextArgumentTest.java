package technology.semi.weaviate.client.v1.graphql.query.argument;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class NearTextArgumentTest extends TestCase {

  @Test
  public void testBuild() {
    // given
    String[] concepts = new String[]{"a", "b", "c"};
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f).build();
    // when
    String arg = nearText.build();
    // then
    Assert.assertEquals("nearText: {concepts: [\"a\", \"b\", \"c\"] certainty: 0.8}", arg);
  }

  @Test
  public void testBuildWithNoConcepts() {
    // given
    String[] concepts = new String[]{"a", "b", "c"};
    NearTextArgument nearText = NearTextArgument.builder()
            .certainty(0.8f).build();
    // when
    String arg = nearText.build();
    // then
    Assert.assertEquals("", arg);
  }

  @Test
  public void testBuildMoveTo() {
    // given
    String[] concepts = new String[]{"a", "b", "c"};
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{"a1", "b2"}).force(0.1f).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f).moveTo(moveTo).build();
    // when
    String arg = nearText.build();
    // then
    Assert.assertEquals("nearText: {concepts: [\"a\", \"b\", \"c\"] certainty: 0.8 moveTo: {concepts: [\"a1\", \"b2\"] force: 0.1}}", arg);
  }

  @Test
  public void testBuildMoveToWithoutForce() {
    // given
    String[] concepts = new String[]{"a", "b", "c"};
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{"a1", "b2"}).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f).moveTo(moveTo).build();
    // when
    String arg = nearText.build();
    // then
    Assert.assertEquals("nearText: {concepts: [\"a\", \"b\", \"c\"] certainty: 0.8 moveTo: {concepts: [\"a1\", \"b2\"]}}", arg);
  }

  @Test
  public void testBuildMoveAwayFrom() {
    // given
    String[] concepts = new String[]{"a", "b", "c"};
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{"a1", "b2"}).force(0.1f).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    Assert.assertEquals("nearText: {concepts: [\"a\", \"b\", \"c\"] certainty: 0.8 moveAwayFrom: {concepts: [\"a1\", \"b2\"] force: 0.1}}", arg);
  }

  @Test
  public void testBuildMoveAwayFromWithoutForce() {
    // given
    String[] concepts = new String[]{"a", "b", "c"};
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{"a1", "b2"}).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    Assert.assertEquals("nearText: {concepts: [\"a\", \"b\", \"c\"] certainty: 0.8 moveAwayFrom: {concepts: [\"a1\", \"b2\"]}}", arg);
  }

  @Test
  public void testBuildMoveToAndMoveAwayFrom() {
    // given
    String[] concepts = new String[]{"a", "b", "c"};
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{"z1", "y2"}).force(0.8f).build();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{"a1", "b2"}).force(0.1f).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f)
            .moveTo(moveTo).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    Assert.assertEquals("nearText: {concepts: [\"a\", \"b\", \"c\"] certainty: 0.8 " +
            "moveTo: {concepts: [\"z1\", \"y2\"] force: 0.8} " +
            "moveAwayFrom: {concepts: [\"a1\", \"b2\"] force: 0.1}}", arg);
  }

  @Test
  public void testBuildMoveToAndMoveAwayFromWithoutForce() {
    // given
    String[] concepts = new String[]{"a", "b", "c"};
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{"z1", "y2"}).force(0.8f).build();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{"a1", "b2"}).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f)
            .moveTo(moveTo).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    Assert.assertEquals("nearText: {concepts: [\"a\", \"b\", \"c\"] certainty: 0.8 " +
            "moveTo: {concepts: [\"z1\", \"y2\"] force: 0.8} " +
            "moveAwayFrom: {concepts: [\"a1\", \"b2\"]}}", arg);
  }
}