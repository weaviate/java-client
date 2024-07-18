package io.weaviate.client.v1.graphql.query.argument;

import java.util.LinkedHashMap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HybridArgumentTest {

  @Test
  public void shouldCreateArgument() {
    HybridArgument hybrid = HybridArgument.builder()
      .query("I'm a simple string")
      .build();

    String str = hybrid.build();

    assertThat(str).isEqualTo("hybrid:{query:\"I'm a simple string\"}");
  }

  @Test
  public void shouldCreateArgumentWithVectorAndAlpha() {
    HybridArgument hybrid = HybridArgument.builder()
      .query("I'm a simple string")
      .vector(new Float[]{.1f, .2f, .3f})
      .alpha(.567f)
      .build();

    String str = hybrid.build();

    assertThat(str).isEqualTo("hybrid:{query:\"I'm a simple string\" " +
      "vector:[0.1,0.2,0.3] " +
      "alpha:0.567}");
  }

  @Test
  public void shouldCreateArgumentWithVectorAndTargetVectors() {
    HybridArgument hybrid = HybridArgument.builder()
      .query("I'm a simple string")
      .vector(new Float[]{.1f, .2f, .3f})
      .targetVectors(new String[]{"vector1"})
      .build();

    String str = hybrid.build();

    assertThat(str).isEqualTo("hybrid:{query:\"I'm a simple string\" " +
      "vector:[0.1,0.2,0.3] " +
      "targetVectors:[\"vector1\"]}");
  }

  @Test
  public void shouldCreateArgumentWithChars() {
    HybridArgument hybrid = HybridArgument.builder()
      .query("\"I'm a complex string\" says the {'`:string:`'}")
      .build();

    String str = hybrid.build();

    assertThat(str).isEqualTo("hybrid:{query:\"\\\"I'm a complex string\\\" says the {'`:string:`'}\"}");
  }

  @Test
  public void shouldCreateArgumentWithFusionTypeRanked() {
    HybridArgument hybrid = HybridArgument.builder()
      .query("I'm a simple string")
      .fusionType(FusionType.RANKED)
      .build();

    String str = hybrid.build();

    assertThat(str).isEqualTo("hybrid:{query:\"I'm a simple string\" " +
      "fusionType:rankedFusion}");
  }

  @Test
  public void shouldCreateArgumentWithFusionTypeRelativeScore() {
    HybridArgument hybrid = HybridArgument.builder()
      .query("I'm a simple string")
      .fusionType(FusionType.RELATIVE_SCORE)
      .build();

    String str = hybrid.build();

    assertThat(str).isEqualTo("hybrid:{query:\"I'm a simple string\" " +
      "fusionType:relativeScoreFusion}");
  }

  @Test
  public void shouldCreateArgumentWithProperties() {
    HybridArgument hybrid = HybridArgument.builder()
      .query("I'm a simple string")
      .properties(new String[]{"prop1", "prop2"})
      .build();

    String str = hybrid.build();

    assertThat(str).isEqualTo("hybrid:{query:\"I'm a simple string\" " +
      "properties:[\"prop1\",\"prop2\"]}");
  }

  @Test
  public void shouldCreateArgumentWithNearVectorSearches() {
    NearVectorArgument nearVector = NearVectorArgument.builder()
      .vector(new Float[]{ .1f, .2f, .3f })
      .certainty(0.9f)
      .build();

    HybridArgument hybrid = HybridArgument.builder()
      .query("I'm a simple string")
      .searches(HybridArgument.Searches.builder().nearVector(nearVector).build())
      .build();

    String str = hybrid.build();

    assertThat(str).isEqualTo("hybrid:{query:\"I'm a simple string\" searches:{nearVector:{vector:[0.1,0.2,0.3] certainty:0.9}}}");
  }

  @Test
  public void shouldCreateArgumentWithNearTextSearches() {
    NearTextArgument nearText = NearTextArgument.builder()
      .concepts(new String[]{"concept"})
      .certainty(0.9f)
      .build();

    HybridArgument hybrid = HybridArgument.builder()
      .query("I'm a simple string")
      .searches(HybridArgument.Searches.builder().nearText(nearText).build())
      .build();

    String str = hybrid.build();

    assertThat(str).isEqualTo("hybrid:{query:\"I'm a simple string\" searches:{nearText:{concepts:[\"concept\"] certainty:0.9}}}");
  }

  @Test
  public void shouldCreateArgumentWithTargets() {
    // given
    LinkedHashMap<String, Float> weights = new LinkedHashMap<>();
    weights.put("t1", 0.8f);
    weights.put("t2", 0.2f);
    Targets targets = Targets.builder()
      .targetVectors(new String[]{ "t1", "t2" })
      .combinationMethod(Targets.CombinationMethod.minimum)
      .weights(weights)
      .build();
    HybridArgument hybrid = HybridArgument.builder()
      .query("I'm a simple string").targets(targets)
      .build();
    // when
    String str = hybrid.build();
    // then
    assertThat(str).isEqualTo("hybrid:{query:\"I'm a simple string\" targets:{combinationMethod:minimum targetVectors:[\"t1\",\"t2\"] weights:{t1:0.8 t2:0.2}}}");
  }
}
