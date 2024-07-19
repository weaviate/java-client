package io.weaviate.client.v1.graphql.query.argument;

import java.util.LinkedHashMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TargetsTest {

  @Test
  public void testBuild() {
    // given
    Targets targets = Targets.builder()
      .targetVectors(new String[]{ "t1", "t2" })
      .combinationMethod(Targets.CombinationMethod.average)
      .build();
    // when
    String targetsStr = targets.build();
    // then
    assertNotNull(targetsStr);
    assertEquals("targets:{combinationMethod:average targetVectors:[\"t1\",\"t2\"]}", targetsStr);
  }

  @Test
  public void testBuildWithWeights() {
    // given
    LinkedHashMap<String, Float> weights = new LinkedHashMap<>();
    weights.put("t1", 0.8f);
    weights.put("t2", 0.2f);
    Targets targets = Targets.builder()
      .targetVectors(new String[]{ "t1", "t2" })
      .combinationMethod(Targets.CombinationMethod.manualWeights)
      .weights(weights)
      .build();
    // when
    String targetsStr = targets.build();
    // then
    assertNotNull(targetsStr);
    assertEquals("targets:{combinationMethod:manualWeights targetVectors:[\"t1\",\"t2\"] weights:{t1:0.8 t2:0.2}}", targetsStr);
  }
}
