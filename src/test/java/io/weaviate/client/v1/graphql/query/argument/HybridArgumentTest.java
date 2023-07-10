package io.weaviate.client.v1.graphql.query.argument;

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
}
