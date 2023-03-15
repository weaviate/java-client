package io.weaviate.client.v1.graphql.query.argument;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class NearTextArgumentTest {

  @Test
  public void testBuildWithCertainty() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8}", arg);
  }

  @Test
  public void testBuildWithDistance() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8}", arg);
  }

  @Test
  public void testBuildWithCertaintyAndNoConcepts() {
    // given
    NearTextArgument nearText = NearTextArgument.builder()
            .certainty(0.8f).build();
    // when
    String arg = nearText.build();
    // then
    // builder will return a faulty nearText arg in order for Weaviate to error
    // so that user will know that something was wrong
    assertEquals("nearText:{certainty:0.8}", arg);
  }

  @Test
  public void testBuildWithDistanceAndNoConcepts() {
    // given
    NearTextArgument nearText = NearTextArgument.builder()
            .distance(0.8f).build();
    // when
    String arg = nearText.build();
    // then
    // builder will return a faulty nearText arg in order for Weaviate to error
    // so that user will know that something was wrong
    assertEquals("nearText:{distance:0.8}", arg);
  }

  @Test
  public void testBuildWithoutAll() {
    // given
    NearTextArgument nearText = NearTextArgument.builder().build();
    // when
    String arg = nearText.build();
    // then
    // builder will return a faulty nearText arg in order for Weaviate to error
    // so that user will know that something was wrong
    assertEquals("nearText:{}", arg);
  }

  @Test
  public void testBuildMoveToWithCertainty() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{ "a1", "b2" }).force(0.1f).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f).moveTo(moveTo).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 moveTo:{concepts:[\"a1\",\"b2\"] force:0.1}}", arg);
  }

  @Test
  public void testBuildMoveToWithDistance() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{ "a1", "b2" }).force(0.1f).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).moveTo(moveTo).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 moveTo:{concepts:[\"a1\",\"b2\"] force:0.1}}", arg);
  }

  @Test
  public void testBuildMoveToWithCertaintyWithoutForce() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{ "a1", "b2" }).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f).moveTo(moveTo).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 moveTo:{concepts:[\"a1\",\"b2\"]}}", arg);
  }

  @Test
  public void testBuildMoveToWithDistanceWithoutForce() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{ "a1", "b2" }).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).moveTo(moveTo).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 moveTo:{concepts:[\"a1\",\"b2\"]}}", arg);
  }

  @Test
  public void testBuildMoveAwayFromWithCertainty() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{ "a1", "b2" }).force(0.1f).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 moveAwayFrom:{concepts:[\"a1\",\"b2\"] force:0.1}}", arg);
  }

  @Test
  public void testBuildMoveAwayFromWithDistance() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{ "a1", "b2" }).force(0.1f).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 moveAwayFrom:{concepts:[\"a1\",\"b2\"] force:0.1}}", arg);
  }

  @Test
  public void testBuildMoveAwayFromWithCertaintyWithoutForce() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{ "a1", "b2" }).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 moveAwayFrom:{concepts:[\"a1\",\"b2\"]}}", arg);
  }

  @Test
  public void testBuildMoveAwayFromWithDistanceWithoutForce() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{ "a1", "b2" }).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 moveAwayFrom:{concepts:[\"a1\",\"b2\"]}}", arg);
  }

  @Test
  public void testBuildMoveToAndMoveAwayFromWithCertainty() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{ "z1", "y2" }).force(0.8f).build();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{ "a1", "b2" }).force(0.1f).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f)
            .moveTo(moveTo).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 " +
            "moveTo:{concepts:[\"z1\",\"y2\"] force:0.8} " +
            "moveAwayFrom:{concepts:[\"a1\",\"b2\"] force:0.1}}", arg);
  }

  @Test
  public void testBuildMoveToAndMoveAwayFromWithDistance() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{ "z1", "y2" }).force(0.8f).build();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{ "a1", "b2" }).force(0.1f).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f)
            .moveTo(moveTo).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 " +
            "moveTo:{concepts:[\"z1\",\"y2\"] force:0.8} " +
            "moveAwayFrom:{concepts:[\"a1\",\"b2\"] force:0.1}}", arg);
  }

  @Test
  public void testBuildMoveToAndMoveAwayFromWithCertaintyWithoutForce() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{ "z1", "y2" }).force(0.8f).build();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{ "a1", "b2" }).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f)
            .moveTo(moveTo).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 " +
            "moveTo:{concepts:[\"z1\",\"y2\"] force:0.8} " +
            "moveAwayFrom:{concepts:[\"a1\",\"b2\"]}}", arg);
  }

  @Test
  public void testBuildMoveToAndMoveAwayFromWithDistanceWithoutForce() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{ "z1", "y2" }).force(0.8f).build();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{ "a1", "b2" }).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f)
            .moveTo(moveTo).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 " +
            "moveTo:{concepts:[\"z1\",\"y2\"] force:0.8} " +
            "moveAwayFrom:{concepts:[\"a1\",\"b2\"]}}", arg);
  }

  @Test
  public void testBuildWithAutocorrectAndCertainty() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f).autocorrect(false).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 autocorrect:false}", arg);
  }

  @Test
  public void testBuildWithAutocorrectAndDistance() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).autocorrect(false).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 autocorrect:false}", arg);
  }

  @Test
  public void testBuildMoveToAndMoveAwayFromWithCertaintyWithoutForceAndWithAutocorrect() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{ "z1", "y2" }).force(0.8f).build();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{ "a1", "b2" }).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).certainty(0.8f).autocorrect(true)
            .moveTo(moveTo).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 " +
            "moveTo:{concepts:[\"z1\",\"y2\"] force:0.8} " +
            "moveAwayFrom:{concepts:[\"a1\",\"b2\"]} autocorrect:true}", arg);
  }

  @Test
  public void testBuildMoveToAndMoveAwayFromWithDistanceWithoutForceAndWithAutocorrect() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
            .concepts(new String[]{ "z1", "y2" }).force(0.8f).build();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
            .concepts(new String[]{ "a1", "b2" }).build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).autocorrect(true)
            .moveTo(moveTo).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 " +
            "moveTo:{concepts:[\"z1\",\"y2\"] force:0.8} " +
            "moveAwayFrom:{concepts:[\"a1\",\"b2\"]} autocorrect:true}", arg);
  }

  @Test
  public void testBuildWithEmptyMoveToOrMoveAway() {
    // given
    NearTextMoveParameters move = NearTextMoveParameters.builder().build();
    NearTextArgument nearText1 = NearTextArgument.builder().moveTo(move).build();
    NearTextArgument nearText2 = NearTextArgument.builder().moveAwayFrom(move).build();
    NearTextArgument nearText3 = NearTextArgument.builder().moveTo(move).moveAwayFrom(move).build();
    // when
    String arg1 = nearText1.build();
    String arg2 = nearText2.build();
    String arg3 = nearText3.build();
    // then
    assertEquals("nearText:{moveTo:{}}", arg1);
    assertEquals("nearText:{moveAwayFrom:{}}", arg2);
    assertEquals("nearText:{moveTo:{} moveAwayFrom:{}}", arg3);
  }

  @Test
  public void testBuildMoveToWithObjectsAndCertainty() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveTo1 = NearTextMoveParameters
      .builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{
        NearTextMoveParameters.ObjectMove.builder().id("uuid").build(),
        NearTextMoveParameters.ObjectMove.builder().beacon("beacon").build()
      })
      .force(0.1f)
      .build();
    NearTextMoveParameters moveTo2 = NearTextMoveParameters
      .builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{
        NearTextMoveParameters.ObjectMove.builder().id("uuid").beacon("beacon").build(),
      })
      .force(0.1f)
      .build();
    NearTextMoveParameters moveTo3 = NearTextMoveParameters
      .builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{
        NearTextMoveParameters.ObjectMove.builder().id("uuid").build(),
      })
      .build();
    NearTextMoveParameters moveTo4 = NearTextMoveParameters
      .builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{
        NearTextMoveParameters.ObjectMove.builder().beacon("beacon").build(),
      })
      .build();
    NearTextArgument nearText1 = NearTextArgument.builder()
      .concepts(concepts).certainty(0.8f).moveTo(moveTo1).build();
    NearTextArgument nearText2 = NearTextArgument.builder()
      .concepts(concepts).certainty(0.8f).moveTo(moveTo2).build();
    NearTextArgument nearText3 = NearTextArgument.builder()
      .concepts(concepts).certainty(0.8f).moveTo(moveTo3).build();
    NearTextArgument nearText4 = NearTextArgument.builder()
      .concepts(concepts).certainty(0.8f).moveTo(moveTo4).build();
    // when
    String arg1 = nearText1.build();
    String arg2 = nearText2.build();
    String arg3 = nearText3.build();
    String arg4 = nearText4.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 moveTo:{force:0.1 objects:[{id:\"uuid\"},{beacon:\"beacon\"}]}}", arg1);
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 moveTo:{force:0.1 objects:[{id:\"uuid\" beacon:\"beacon\"}]}}", arg2);
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 moveTo:{objects:[{id:\"uuid\"}]}}", arg3);
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 moveTo:{objects:[{beacon:\"beacon\"}]}}", arg4);
  }

  @Test
  public void testBuildMoveToWithObjectsAndDistance() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveTo1 = NearTextMoveParameters
            .builder()
            .objects(new NearTextMoveParameters.ObjectMove[]{
                    NearTextMoveParameters.ObjectMove.builder().id("uuid").build(),
                    NearTextMoveParameters.ObjectMove.builder().beacon("beacon").build()
            })
            .force(0.1f)
            .build();
    NearTextMoveParameters moveTo2 = NearTextMoveParameters
            .builder()
            .objects(new NearTextMoveParameters.ObjectMove[]{
                    NearTextMoveParameters.ObjectMove.builder().id("uuid").beacon("beacon").build(),
            })
            .force(0.1f)
            .build();
    NearTextMoveParameters moveTo3 = NearTextMoveParameters
            .builder()
            .objects(new NearTextMoveParameters.ObjectMove[]{
                    NearTextMoveParameters.ObjectMove.builder().id("uuid").build(),
            })
            .build();
    NearTextMoveParameters moveTo4 = NearTextMoveParameters
            .builder()
            .objects(new NearTextMoveParameters.ObjectMove[]{
                    NearTextMoveParameters.ObjectMove.builder().beacon("beacon").build(),
            })
            .build();
    NearTextArgument nearText1 = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).moveTo(moveTo1).build();
    NearTextArgument nearText2 = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).moveTo(moveTo2).build();
    NearTextArgument nearText3 = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).moveTo(moveTo3).build();
    NearTextArgument nearText4 = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).moveTo(moveTo4).build();
    // when
    String arg1 = nearText1.build();
    String arg2 = nearText2.build();
    String arg3 = nearText3.build();
    String arg4 = nearText4.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 moveTo:{force:0.1 objects:[{id:\"uuid\"},{beacon:\"beacon\"}]}}", arg1);
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 moveTo:{force:0.1 objects:[{id:\"uuid\" beacon:\"beacon\"}]}}", arg2);
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 moveTo:{objects:[{id:\"uuid\"}]}}", arg3);
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 moveTo:{objects:[{beacon:\"beacon\"}]}}", arg4);
  }

  @Test
  public void testBuildMoveAwayWithObjectsAndCertainty() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveAwayFrom1 = NearTextMoveParameters
      .builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{
        NearTextMoveParameters.ObjectMove.builder().id("uuid").build(),
        NearTextMoveParameters.ObjectMove.builder().beacon("beacon").build()
      })
      .force(0.1f)
      .build();
    NearTextMoveParameters moveAwayFrom2 = NearTextMoveParameters
      .builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{
        NearTextMoveParameters.ObjectMove.builder().id("uuid").beacon("beacon").build(),
      })
      .force(0.1f)
      .build();
    NearTextMoveParameters moveAwayFrom3 = NearTextMoveParameters
      .builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{
        NearTextMoveParameters.ObjectMove.builder().id("uuid").build(),
      })
      .build();
    NearTextMoveParameters moveAwayFrom4 = NearTextMoveParameters
      .builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{
        NearTextMoveParameters.ObjectMove.builder().beacon("beacon").build(),
      })
      .build();
    NearTextArgument nearText1 = NearTextArgument.builder()
      .concepts(concepts).certainty(0.8f).moveAwayFrom(moveAwayFrom1).build();
    NearTextArgument nearText2 = NearTextArgument.builder()
      .concepts(concepts).certainty(0.8f).moveAwayFrom(moveAwayFrom2).build();
    NearTextArgument nearText3 = NearTextArgument.builder()
      .concepts(concepts).certainty(0.8f).moveAwayFrom(moveAwayFrom3).build();
    NearTextArgument nearText4 = NearTextArgument.builder()
      .concepts(concepts).certainty(0.8f).moveAwayFrom(moveAwayFrom4).build();
    // when
    String arg1 = nearText1.build();
    String arg2 = nearText2.build();
    String arg3 = nearText3.build();
    String arg4 = nearText4.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 moveAwayFrom:{force:0.1 objects:[{id:\"uuid\"},{beacon:\"beacon\"}]}}", arg1);
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 moveAwayFrom:{force:0.1 objects:[{id:\"uuid\" beacon:\"beacon\"}]}}", arg2);
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 moveAwayFrom:{objects:[{id:\"uuid\"}]}}", arg3);
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 moveAwayFrom:{objects:[{beacon:\"beacon\"}]}}", arg4);
  }

  @Test
  public void testBuildMoveAwayWithObjectsAndDistance() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveAwayFrom1 = NearTextMoveParameters
            .builder()
            .objects(new NearTextMoveParameters.ObjectMove[]{
                    NearTextMoveParameters.ObjectMove.builder().id("uuid").build(),
                    NearTextMoveParameters.ObjectMove.builder().beacon("beacon").build()
            })
            .force(0.1f)
            .build();
    NearTextMoveParameters moveAwayFrom2 = NearTextMoveParameters
            .builder()
            .objects(new NearTextMoveParameters.ObjectMove[]{
                    NearTextMoveParameters.ObjectMove.builder().id("uuid").beacon("beacon").build(),
            })
            .force(0.1f)
            .build();
    NearTextMoveParameters moveAwayFrom3 = NearTextMoveParameters
            .builder()
            .objects(new NearTextMoveParameters.ObjectMove[]{
                    NearTextMoveParameters.ObjectMove.builder().id("uuid").build(),
            })
            .build();
    NearTextMoveParameters moveAwayFrom4 = NearTextMoveParameters
            .builder()
            .objects(new NearTextMoveParameters.ObjectMove[]{
                    NearTextMoveParameters.ObjectMove.builder().beacon("beacon").build(),
            })
            .build();
    NearTextArgument nearText1 = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).moveAwayFrom(moveAwayFrom1).build();
    NearTextArgument nearText2 = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).moveAwayFrom(moveAwayFrom2).build();
    NearTextArgument nearText3 = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).moveAwayFrom(moveAwayFrom3).build();
    NearTextArgument nearText4 = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).moveAwayFrom(moveAwayFrom4).build();
    // when
    String arg1 = nearText1.build();
    String arg2 = nearText2.build();
    String arg3 = nearText3.build();
    String arg4 = nearText4.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 moveAwayFrom:{force:0.1 objects:[{id:\"uuid\"},{beacon:\"beacon\"}]}}", arg1);
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 moveAwayFrom:{force:0.1 objects:[{id:\"uuid\" beacon:\"beacon\"}]}}", arg2);
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 moveAwayFrom:{objects:[{id:\"uuid\"}]}}", arg3);
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 moveAwayFrom:{objects:[{beacon:\"beacon\"}]}}", arg4);
  }

  @Test
  public void testBuildMoveToAndMoveAwayFromWithObjectsAndCertainty() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveTo = NearTextMoveParameters
      .builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{
        NearTextMoveParameters.ObjectMove.builder().id("uuid").build(),
        NearTextMoveParameters.ObjectMove.builder().beacon("beacon").build()
      })
      .force(0.1f)
      .build();
    NearTextMoveParameters moveAwayFrom = NearTextMoveParameters
      .builder()
      .objects(new NearTextMoveParameters.ObjectMove[]{
        NearTextMoveParameters.ObjectMove.builder().id("uuid").beacon("beacon").build(),
      })
      .force(0.2f)
      .build();
    NearTextArgument nearText1 = NearTextArgument.builder()
      .concepts(concepts).certainty(0.8f).moveTo(moveTo).moveAwayFrom(moveAwayFrom).build();
    // when
    String arg1 = nearText1.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] certainty:0.8 moveTo:{force:0.1 objects:[{id:\"uuid\"},{beacon:\"beacon\"}]} " +
      "moveAwayFrom:{force:0.2 objects:[{id:\"uuid\" beacon:\"beacon\"}]}}", arg1);
  }

  @Test
  public void testBuildMoveToAndMoveAwayFromWithObjectsAndDistance() {
    // given
    String[] concepts = new String[]{ "a", "b", "c" };
    NearTextMoveParameters moveTo = NearTextMoveParameters
            .builder()
            .objects(new NearTextMoveParameters.ObjectMove[]{
                    NearTextMoveParameters.ObjectMove.builder().id("uuid").build(),
                    NearTextMoveParameters.ObjectMove.builder().beacon("beacon").build()
            })
            .force(0.1f)
            .build();
    NearTextMoveParameters moveAwayFrom = NearTextMoveParameters
            .builder()
            .objects(new NearTextMoveParameters.ObjectMove[]{
                    NearTextMoveParameters.ObjectMove.builder().id("uuid").beacon("beacon").build(),
            })
            .force(0.2f)
            .build();
    NearTextArgument nearText1 = NearTextArgument.builder()
            .concepts(concepts).distance(0.8f).moveTo(moveTo).moveAwayFrom(moveAwayFrom).build();
    // when
    String arg1 = nearText1.build();
    // then
    assertEquals("nearText:{concepts:[\"a\",\"b\",\"c\"] distance:0.8 moveTo:{force:0.1 objects:[{id:\"uuid\"},{beacon:\"beacon\"}]} " +
            "moveAwayFrom:{force:0.2 objects:[{id:\"uuid\" beacon:\"beacon\"}]}}", arg1);
  }

  @Test
  public void shouldCreateArgumentWithChars() {
    // given
    String[] concepts = new String[]{ "\"I'm a complex\" {'`:concept:`'}", "b" };
    NearTextMoveParameters moveTo = NearTextMoveParameters.builder()
      .concepts(new String[]{ "\"I'm a another complex\" {'`:concept:`'}", "y2" }).force(0.8f).build();
    NearTextMoveParameters moveAway = NearTextMoveParameters.builder()
      .concepts(new String[]{ "\"I'm a yet another complex\" {'`:concept:`'}", "b2" }).force(0.1f).build();
    NearTextArgument nearText = NearTextArgument.builder()
      .concepts(concepts).certainty(0.8f)
      .moveTo(moveTo).moveAwayFrom(moveAway).build();
    // when
    String arg = nearText.build();
    // then
    assertThat(arg).isEqualTo("nearText:{concepts:[\"\\\"I'm a complex\\\" {'`:concept:`'}\",\"b\"] certainty:0.8 " +
      "moveTo:{concepts:[\"\\\"I'm a another complex\\\" {'`:concept:`'}\",\"y2\"] force:0.8} " +
      "moveAwayFrom:{concepts:[\"\\\"I'm a yet another complex\\\" {'`:concept:`'}\",\"b2\"] force:0.1}}");
  }
}
