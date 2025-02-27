package io.weaviate.client.v1.graphql.query.argument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class NearVectorArgumentTest {

  @Test
  public void testBuildWithCertainty() {
    // given
    NearVectorArgument nearVector = NearVectorArgument.builder()
        .vector(new Float[] { 1f, 2f, 3f }).certainty(0.8f).build();
    // when
    String arg = nearVector.build();
    // then
    assertEquals("nearVector:{vector:[1.0,2.0,3.0] certainty:0.8}", arg);
  }

  @Test
  public void testBuildWithDistance() {
    // given
    NearVectorArgument nearVector = NearVectorArgument.builder()
        .vector(new Float[] { 1f, 2f, 3f }).distance(0.8f).build();
    // when
    String arg = nearVector.build();
    // then
    assertEquals("nearVector:{vector:[1.0,2.0,3.0] distance:0.8}", arg);
  }

  @Test
  public void testBuildWithNoCertainty() {
    NearVectorArgument nearVector = NearVectorArgument.builder()
        .vector(new Float[] { 1f, 2f, 3f }).build();
    // when
    String arg = nearVector.build();
    // then
    assertEquals("nearVector:{vector:[1.0,2.0,3.0]}", arg);
  }

  @Test
  public void shouldBuildWithTargetVectors() {
    String nearVector = NearVectorArgument.builder()
        .vector(new Float[] { 1f, 2f, 3f })
        .targetVectors(new String[] { "vector1" })
        .build().build();

    assertThat(nearVector).isEqualTo("nearVector:{vector:[1.0,2.0,3.0] targetVectors:[\"vector1\"]}");
  }

  @Test
  public void testBuildWithTargets() {
    // given
    LinkedHashMap<String, Float[]> vectorPerTarget = new LinkedHashMap<>();
    vectorPerTarget.put("t1", new Float[] { 1f, 2f, 3f });
    vectorPerTarget.put("t2", new Float[] { .1f, .2f, .3f });
    LinkedHashMap<String, Float> weights = new LinkedHashMap<>();
    weights.put("t1", 0.8f);
    weights.put("t2", 0.2f);
    Targets targets = Targets.builder()
        .targetVectors(new String[] { "t1", "t2" })
        .combinationMethod(Targets.CombinationMethod.sum)
        .weights(weights)
        .build();
    NearVectorArgument nearVector = NearVectorArgument.builder()
        .vectorPerTarget(vectorPerTarget)
        .targets(targets)
        .build();
    // when
    String arg = nearVector.build();
    // then
    assertEquals(
        "nearVector:{vectorPerTarget:{t1:[1.0,2.0,3.0] t2:[0.1,0.2,0.3]} targets:{combinationMethod:sum targetVectors:[\"t1\",\"t2\"] weights:{t1:0.8 t2:0.2}}}",
        arg);
  }

  @Test
  public void testBuildWithMultipleVectorsPerTarget() {
    Map<String, Float[][]> vectorsPerTarget = new LinkedHashMap<String, Float[][]>() {
      {
        this.put("t1", new Float[][] { new Float[] { 1f, 2f, 3f }, new Float[] { 4f, 5f, 6f } });
        this.put("t2", new Float[][] { new Float[] { .1f, .2f, .3f } });
      }
    };
    NearVectorArgument nearVector = NearVectorArgument.builder()
        .targets(Targets.builder().targetVectors(new String[] { "t1", "t2" }).build())
        .vectorsPerTarget(vectorsPerTarget).build();

    String got = nearVector.build();

    assertEquals(
        "nearVector:{vectorPerTarget:{t1:[[1.0,2.0,3.0],[4.0,5.0,6.0]] t2:[0.1,0.2,0.3]} targets:{targetVectors:[\"t1\",\"t1\",\"t2\"]}}",
        got);
  }

  @Test
  public void testBuildWithColBERTVectorsAndTarget() {
    NearVectorArgument nearVector = NearVectorArgument.builder()
        .targetVectors(new String[] { "colbert" })
        .vector(new Float[][] { { 1f, 2f, 3f }, { 4f, 5f, 6f } })
        .build();

    String got = nearVector.build();

    assertEquals(
        "nearVector:{vector:[[1.0,2.0,3.0],[4.0,5.0,6.0]] targetVectors:[\"colbert\"]}",
        got);
  }
}
